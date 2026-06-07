package edu.icesi.sitmmio.concurrent;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public final class PartitionerTest {

    @Test
    public void testSplitEmpty() {
        Partitioner partitioner = new Partitioner();
        List<List<Map<String, String>>> partitions = partitioner.split(new ArrayList<>(), 4);
        assertFalse(partitions.isEmpty());
        assertEquals(1, partitions.size());
        assertTrue(partitions.get(0).isEmpty());
    }

    @Test
    public void testSplitNormal() {
        Partitioner partitioner = new Partitioner();
        List<Map<String, String>> rows = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            rows.add(Map.of("id", String.valueOf(i)));
        }

        List<List<Map<String, String>>> partitions = partitioner.split(rows, 3);
        assertEquals(3, partitions.size());
        assertEquals(4, partitions.get(0).size()); // Ceil(10/3) = 4
        assertEquals(4, partitions.get(1).size());
        assertEquals(2, partitions.get(2).size());
    }

    @Test
    public void testSerializationOfPartitions() throws Exception {
        Partitioner partitioner = new Partitioner();
        List<Map<String, String>> rows = new ArrayList<>();
        rows.add(Map.of("key", "value"));

        List<List<Map<String, String>>> partitions = partitioner.split(rows, 1);
        List<Map<String, String>> partition = partitions.get(0);

        // Intenta serializar la particion. Deberia completarse sin lanzar NotSerializableException
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(buffer)) {
            out.writeObject(partition);
        }
        assertTrue(buffer.size() > 0);
    }
}
