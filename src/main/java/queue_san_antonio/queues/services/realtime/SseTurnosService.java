package queue_san_antonio.queues.services.realtime;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

@Service
public class SseTurnosService {

    private final Map<Long, Set<SseEmitter>> emittersBySector = new ConcurrentHashMap<>();
    private static final long TIMEOUT_MS = 0L; // 0 = sin timeout (o usá 30 * 60_000)

    public SseEmitter subscribe(Long sectorId) {
        SseEmitter emitter = new SseEmitter(TIMEOUT_MS);
        emittersBySector.computeIfAbsent(sectorId, k -> ConcurrentHashMap.newKeySet()).add(emitter);

        Runnable cleanup = () -> {
            Set<SseEmitter> set = emittersBySector.getOrDefault(sectorId, Collections.emptySet());
            set.remove(emitter);
        };
        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(e -> cleanup.run());

        // “hola” inicial
        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data(Map.of("ts", Instant.now().toString(), "sectorId", sectorId)));
        } catch (IOException e) {
            cleanup.run();
        }
        return emitter;
    }

    public void notifySector(Long sectorId, String eventName, Object payload) {
        var set = emittersBySector.get(sectorId);
        if (set == null || set.isEmpty()) return;

        List<SseEmitter> toRemove = new ArrayList<>();
        for (SseEmitter em : set) {
            try {
                em.send(SseEmitter.event().name(eventName).data(payload));
            } catch (IOException ex) {
                toRemove.add(em);
            }
        }
        toRemove.forEach(em -> emittersBySector.getOrDefault(sectorId, Collections.emptySet()).remove(em));
    }
}
