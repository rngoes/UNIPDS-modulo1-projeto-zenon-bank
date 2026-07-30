package br.com.zenon.fraud;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class Main {

    void main() {
        var transactionIngestor = new TransactionIngestor();
        List<Transaction> transactions = transactionIngestor.read("data/PS_20174392719_1491204439457_log.csv");
       IO.println(transactions.size());
    //    transactions.stream().limit(10).forEach(IO::println);

    //    IO.println("-----------------------------------");

    //    List<Transaction> transactionsBadData = transactionIngestor.read("data/paysim_with_bad_data.txt");
    //    IO.println(transactionsBadData.size());
    //    transactionsBadData.forEach(IO::println);

        IO.println("-----------------------------------");

        var fraudAnalyzer = new FraudAnalyzer(transactions);

        // Apenas transações onde isFraud == true, imprima o tamanho da lista
        long fraudCounts = fraudAnalyzer.countFrauds();
        IO.println("1. Total de Fraudes: " + fraudCounts);

        // Imprima as 3 fraudes de maior valor (amount)
        List<BigDecimal> highestFraudsAmounts =  fraudAnalyzer.findHighestValueFraudsAmounts(3);
        IO.println("2. Top 3 Fraudes de Maior Valor:");
        highestFraudsAmounts.forEach(amount -> IO.println("- %.2f".formatted(amount)));

        // Obter apenas os nomes dos clientes de origem (nameOrig) dessas fraudes e depois gere uma lista sem repetições (Set ou distinct) com os 5 maiores clientes suspeitos.
        List<String> suspiciousCLients = fraudAnalyzer.findTopSuspiciousClients(5);
        IO.println("3. Clientes Suspeitos:");
        suspiciousCLients.forEach(IO::println);

        // Calcule o prejuízo total causado pelas fraudes (soma dos amount).
        BigDecimal totalFraudLoss = fraudAnalyzer.calculateTotalFraudLoss();
        IO.println("4. Prejuízo Total: " + totalFraudLoss);

        // Conte quantas fraudes ocorreram por tipo de transação (CASH_OUT, TRANSFER, etc...).
        Map<TransactionType, Long> fraudCountByType = fraudAnalyzer.countFraudsByType();
        IO.println("5. Fraudes por Tipo:");
        fraudCountByType.forEach((type, count) -> IO.println("- %s: %d".formatted(type, count)));

        IO.println("-----------------------------------");

        TransactionRepository transactionRepository;

        transactionRepository = new TransactionListRepository(transactions);
        String notFoundOriginName = "C12345";
        transactionRepository.findByOriginName(notFoundOriginName)
                .ifPresentOrElse(IO::println, () -> IO.println("Transação não encontrada para o valor " + notFoundOriginName));

        String existingOriginName = "C1868032458";
        long statTimeList = System.nanoTime();
        transactionRepository.findByOriginName(existingOriginName)
                .ifPresentOrElse(IO::println, () -> IO.println("Transação não encontrada para o valor " + existingOriginName));
        long endTimeList = System.nanoTime();
        IO.println("Tempo de busca para list em ms: " + (endTimeList - statTimeList)/1_000_000.0);

        transactionRepository = new TransactionMapRepository(transactions);
        long statTimeMap = System.nanoTime();
        transactionRepository.findByOriginName(existingOriginName)
             .ifPresentOrElse(IO::println, () -> IO.println("Transação não encontrada para o valor " + existingOriginName));
        long endTimeMap = System.nanoTime();
        IO.println("Tempo de busca para Map em ms: " + (endTimeMap - statTimeMap)/1_000_000.0);
    }
}