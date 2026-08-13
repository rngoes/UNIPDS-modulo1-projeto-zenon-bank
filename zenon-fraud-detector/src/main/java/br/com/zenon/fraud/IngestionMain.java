package br.com.zenon.fraud;

import java.util.List;

public class IngestionMain {

    void main() {

        var repository = new TransactionSQLRepository();
        var transactionIngestor = new EfficientTransactionIngestor();

        long statTimeSQL = System.nanoTime();
        transactionIngestor.readAsBatch("data/PS_20174392719_1491204439457_log.csv", repository::saveAll);
         IO.println("Iniciando adição das trasações no BD...");
        long endTimeSQL = System.nanoTime();
        IO.println("Tempo de ingestão no BD (ms): " + (endTimeSQL - statTimeSQL)/1_000_000.0);

        //repository.findByOriginName("C1231006815")
        //        .ifPresentOrElse(IO::println,() -> IO.println("Transação não Encontrada"));

    }

}
