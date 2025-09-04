package queue_san_antonio.queues.services.impl;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import queue_san_antonio.queues.models.EstadoTurno;
import queue_san_antonio.queues.models.HorarioAtencion;
import queue_san_antonio.queues.repositories.HorarioAtencionRepository;
import queue_san_antonio.queues.repositories.TurnoRepository;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DisponibilidadService {

    private final HorarioAtencionRepository horarioRepo;
    private final TurnoRepository turnoRepo;

    public List<String> getHorasDisponibles(Long sectorId, LocalDate fecha) {
        DayOfWeek dow = fecha.getDayOfWeek();

        // 1) Bloques activos ese día
        List<HorarioAtencion> bloques = horarioRepo.findActivosBySectorAndDia(sectorId, dow);
        if (bloques.isEmpty()) return List.of();

        // 2) Generar slots teóricos HH:mm + capacidad por slot (suma si hay superposición)
        List<String> slots = new ArrayList<>();
        Map<String, Integer> capacidadPorSlot = new HashMap<>();

        for (var h : bloques) {
            LocalTime start = h.getHoraInicio();
            LocalTime end   = h.getHoraFin();
            Duration step   = Duration.ofMinutes(h.getIntervaloCitas());
            int cap         = Optional.ofNullable(h.getCapacidadMaxima()).orElse(1);

            for (LocalTime t = start; !t.isAfter(end.minus(step)); t = t.plus(step)) {
                String hhmm = t.toString().substring(0, 5);
                slots.add(hhmm);
                capacidadPorSlot.merge(hhmm, cap, Integer::sum);
            }
        }

        // 3) Ocupación real del día (excluyendo estados que NO ocupan cupo)
        var excluir = List.of(
                EstadoTurno.CANCELADO,
                EstadoTurno.AUSENTE,
                EstadoTurno.FINALIZADO
        );
        var counts = turnoRepo.countByHoraForSectorAndFecha(sectorId, fecha, excluir);

        Map<String, Integer> ocupadosPorSlot = new HashMap<>();
        for (Object[] row : counts) {
            LocalTime hora = (LocalTime) row[0];
            Number cant = (Number) row[1];
            String hhmm = hora.toString().substring(0, 5);
            ocupadosPorSlot.merge(hhmm, cant.intValue(), Integer::sum);
        }

        // 4) Devolver sólo slots con cupo disponible
        return slots.stream()
                .distinct()
                .sorted()
                .filter(hhmm -> ocupadosPorSlot.getOrDefault(hhmm, 0)
                        < capacidadPorSlot.getOrDefault(hhmm, 1))
                .collect(Collectors.toList());
    }
}
