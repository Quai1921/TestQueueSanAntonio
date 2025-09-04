package queue_san_antonio.queues.utils;

import java.time.DayOfWeek;
import java.util.Map;

public class DiaSemanaUtil {

    private static final Map<DayOfWeek, String> DIAS_SEMANA_ESPANOL = Map.of(
            DayOfWeek.MONDAY, "Lunes",
            DayOfWeek.TUESDAY, "Martes",
            DayOfWeek.WEDNESDAY, "Miércoles",
            DayOfWeek.THURSDAY, "Jueves",
            DayOfWeek.FRIDAY, "Viernes",
            DayOfWeek.SATURDAY, "Sábado",
            DayOfWeek.SUNDAY, "Domingo"
    );

    // Convierte un DayOfWeek a su nombre en español
    // @param dayOfWeek día de la semana
    // @return nombre en español
    public static String toEspanol(DayOfWeek dayOfWeek) {
        return DIAS_SEMANA_ESPANOL.getOrDefault(dayOfWeek, dayOfWeek.name());
    }
}
