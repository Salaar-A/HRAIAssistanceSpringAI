package com.ai.hraiassistance.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class FileLoader implements CommandLineRunner {

    private final VectorStore vectorStore;

    @Value("classpath:/docs/employeedata.csv")
    private Resource employeeData;

    public FileLoader(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @Override
    public void run(String... args) throws Exception {
        List<Document> pages = new TextReader(employeeData).read();  // 1. read
        List<Document> chunks = new TokenTextSplitter().split(pages);   // 2. split
        vectorStore.add(chunks);                                        // 3. store
        log.info("Loaded chunks:{}",chunks.size());
    }
}
