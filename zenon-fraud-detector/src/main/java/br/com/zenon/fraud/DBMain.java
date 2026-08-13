package br.com.zenon.fraud;

import java.util.List;

public class DBMain {

    void main() {

        ConnectionFactory.getConnection();
        IO.println("Coneção com o BD criada.");

        var repository = new TransactionSQLRepository();
        var transactionIngestor = new TransactionIngestor();

        long statTimeSQL = System.nanoTime();
        List<Transaction> transactions = transactionIngestor.read("data/PS_20174392719_1491204439457_log.csv");
        IO.println(transactions.size());
        IO.println("Iniciando adição das trasações no BD...");
        transactions.forEach(repository::save);
        long endTimeSQL = System.nanoTime();
        IO.println("Tempo de insersão para list em ms: " + (endTimeSQL - statTimeSQL)/1_000_000.0);

        repository.findByOriginName("C1231006815")
                .ifPresentOrElse(IO::println,() -> IO.println("Transação não Encontrada"));

    }

}
