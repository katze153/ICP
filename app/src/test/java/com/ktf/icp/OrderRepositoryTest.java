package com.ktf.icp;

import com.ktf.icp.model.CalculationResult;
import com.ktf.icp.model.OrderData;
import com.ktf.icp.repository.OrderRepository;

import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.*;

public class OrderRepositoryTest {

    @Test
    public void createReadDeleteOrder() throws Exception {
        Path tmp = Files.createTempDirectory("icp-test").resolve("save.toml");
        OrderRepository repository = new OrderRepository(tmp);

        OrderData data = new OrderData("A1", 10.0, 40.0, InterestCalculator.METHOD_YEARLY, "09.08.2026");
        assertTrue(repository.save(data));
        assertTrue(repository.exists("A1"));

        OrderData loaded = repository.get("A1");
        assertNotNull(loaded);
        assertEquals(10.0, loaded.getMoney(), 0.001);
        assertEquals(40.0, loaded.getZins(), 0.001);
        assertEquals(InterestCalculator.METHOD_YEARLY, loaded.getMethode());
        assertEquals("09.08.2026", loaded.getDate());

        CalculationResult result = repository.calculateCurrentResult(loaded, LocalDate.of(2026, 8, 19), new InterestCalculator());
        assertEquals(10, result.getDays());

        assertTrue(repository.delete("A1"));
        assertFalse(repository.exists("A1"));
    }

    @Test
    public void handlesMultipleOrdersAndCompatibilityFile() throws Exception {
        Path tmp = Files.createTempDirectory("icp-test").resolve("save.toml");
        Files.writeString(tmp, "[One]\nmoney=10.0\nzins=40.0\nmethode=2\ndate=09.08.2026\n\n[Two]\nmoney=5.5\nzins=2.5\nmethode=1\ndate=10.08.2026\n");

        OrderRepository repository = new OrderRepository(tmp);
        List<String> names = repository.getAllOrderNames();
        assertEquals(2, names.size());
        assertNotNull(repository.get("One"));
        assertNotNull(repository.get("Two"));
    }

    @Test
    public void handlesMissingSaveToml() throws Exception {
        Path tmp = Files.createTempDirectory("icp-test").resolve("save.toml");
        OrderRepository repository = new OrderRepository(tmp);
        assertTrue(repository.getAllOrderNames().isEmpty());
        assertNull(repository.get("none"));
    }
}
