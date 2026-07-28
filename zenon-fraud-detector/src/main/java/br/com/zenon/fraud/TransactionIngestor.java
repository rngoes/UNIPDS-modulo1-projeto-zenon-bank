package br.com.zenon.fraud;

import java.io.FileInputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TransactionIngestor {

    public List<Transaction> read(String filename) {
        Path path = Path.of(filename);
        try {
            List<String> lines = Files.readAllLines(path);
            return lines.stream()
                    .skip(1)
                    .limit(1000)
                    .map(this::parseTransaction)
                    .toList();

        } catch ( Exception ex ) {
            throw new RuntimeException("Erro ao ler o arquivo: " + filename, ex);
        }
    }

    public List<Transaction> readOld(String filename) {

        List<Transaction> transactions = new ArrayList<>();
        try(FileInputStream fis = new FileInputStream(filename);
            Scanner scanner = new Scanner(fis)) {

            int lineCount = 0;
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                lineCount++;

                if (lineCount == 1){
                    continue;
                }
                if (lineCount > 1001) {
                    break;
                }

                var transaction = parseTransaction(line);
                transactions.add(transaction);
            }

        } catch ( Exception ex ) {
            throw new RuntimeException("Erro ao ler o arquivo: " + filename, ex);
        }

        return transactions;
    }

    private Transaction parseTransaction(String line) {
        String[] chunks = line.split(",");

        int step = Integer.parseInt(chunks[0]);
        TransactionType type = TransactionType.valueOf(chunks[1]);
        BigDecimal amount = new BigDecimal(chunks[2]);
        var origin = new TransactionCustumer(chunks[3], new BigDecimal(chunks[4]), new BigDecimal(chunks[5]));
        var recipient = new TransactionCustumer(chunks[6], new BigDecimal(chunks[7]), new BigDecimal(chunks[8]));
        boolean isFraud = "1".equals(chunks[9]);
        boolean isFlaggedFraud = "1".equals(chunks[10]);

        return new Transaction(step, type, amount, origin, recipient, isFraud, isFlaggedFraud);
    }
}
