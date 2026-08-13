package br.com.zenon.fraud;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class EfficientTransactionIngestor {

    private static final int LINE_BATCH_SIZE = 2_500;

    public void readAsBatch(String filename, Consumer<List<Transaction>> batchConsumer) {
        Path path = Path.of(filename);
        try (ExecutorService executor = Executors.newFixedThreadPool(10);
             Stream<String> lines = Files.lines(path).skip(1)) {
            var itaretor = lines.iterator();

            List<String> lineBatch = new ArrayList<>(LINE_BATCH_SIZE);
            while(itaretor.hasNext()) {
                String line = itaretor.next();
                lineBatch.add(line);

                if (lineBatch.size() >= LINE_BATCH_SIZE) {
                    IO.println("Executando o batch ingestor...");
                    final List<String> currentiLineBatch = List.copyOf(lineBatch);
                    executor.submit(() -> executeBatch(currentiLineBatch, batchConsumer));
                    lineBatch.clear();
                }
            }

            if (!lineBatch.isEmpty()) {
                IO.println("Executando o batch final ingestor...");
                final List<String> currentiLineBatch = List.copyOf(lineBatch);
                executor.submit(() -> executeBatch(currentiLineBatch, batchConsumer));
                lineBatch.clear();
            }

        } catch ( Exception ex ) {
            throw new RuntimeException("Erro ao ler o arquivo: " + filename, ex);
        }
    }

    private void executeBatch(List<String> lineBatch, Consumer<List<Transaction>> batchConsumer) {
        List<Transaction> transactionsBatch = lineBatch
                .stream()
                .map(this::parseTransaction)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
        batchConsumer.accept(transactionsBatch);
    }

    public void readAsStream(String filename, Consumer<Transaction> consumer) {
        Path path = Path.of(filename);
        try (Stream<String> lines = Files.lines(path)) {
            lines
                .skip(1)
                //.limit(FRAUD_LIMIT)
                .map(this::parseTransaction)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(consumer);

        } catch ( Exception ex ) {
            throw new RuntimeException("Erro ao ler o arquivo: " + filename, ex);
        }
    }

    private Optional<Transaction> parseTransaction(String line) {
        try {
            String[] chunks = line.split(",");

            int step = Integer.parseInt(chunks[0]);
            TransactionType type = TransactionType.valueOf(chunks[1]);
            if (chunks[2] == null || chunks[2].trim().isEmpty()) throw new IllegalArgumentException("O valor de amount não pode ser nulo nem vazio");
            BigDecimal amount = new BigDecimal(chunks[2]);
            if (chunks[4] == null || chunks[4].trim().isEmpty()) throw new IllegalArgumentException("O valor de oldBalanceOrigin não pode ser nulo nem vazio");
            if (chunks[5] == null || chunks[5].trim().isEmpty()) throw new IllegalArgumentException("O valor de newBalanceOrigin não pode ser nulo nem vazio");
            var origin = new TransactionCustumer(chunks[3], new BigDecimal(chunks[4]), new BigDecimal(chunks[5]));
            if (chunks[4] == null || chunks[7].trim().isEmpty()) throw new IllegalArgumentException("O valor de oldBalanceRecipient não pode ser nulo nem vazio");
            if (chunks[5] == null || chunks[8].trim().isEmpty()) throw new IllegalArgumentException("O valor de newBalanceRecipient não pode ser nulo nem vazio");
            var recipient = new TransactionCustumer(chunks[6], new BigDecimal(chunks[7]), new BigDecimal(chunks[8]));
            boolean isFraud = "1".equals(chunks[9]);
            boolean isFlaggedFraud = "1".equals(chunks[10]);

            return Optional.of(new Transaction(step, type, amount, origin, recipient, isFraud, isFlaggedFraud));
        } catch ( Exception e ) {
            System.err.println("Erro ao fazer o parse: " + line + " | " + e);
            return Optional.empty();
        }

    }
}
