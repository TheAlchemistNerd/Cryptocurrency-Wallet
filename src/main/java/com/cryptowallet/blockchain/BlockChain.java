package com.cryptowallet.blockchain;

import com.cryptowallet.merkle.MerkleTreeBuilder;
import com.cryptowallet.model.Block;
import com.cryptowallet.repository.BlockRepository;
import com.cryptowallet.repository.TransactionRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manages the conceptual blockchain, including adding new blocks and verifying the chain integrity.
 * This class applies a conceptual "Chain of Responsibility" for block processing,
 * where 'addBlock' method handles the sequence: Merkle root calculation, block construction, and persistence.
 */
@Service
@Slf4j
public class BlockChain {

    private List<Block> chain; // In-memory representation of the chain
    private final BlockRepository blockRepository;
    private final MerkleTreeBuilder merkleTreeBuilder;
    private final TransactionRepository transactionRepository;

    // A configurable genesis block hash for initialization
    @Value("${blockchain.genesis.hash:GENESIS_BLOCK_HASH_DEFAULT}")
    private String genesisBlockHash;

    public BlockChain(BlockRepository blockRepository,
                      MerkleTreeBuilder merkleTreeBuilder,
                      TransactionRepository transactionRepository) {
        this.blockRepository = blockRepository;
        this.merkleTreeBuilder = merkleTreeBuilder;
        this.transactionRepository = transactionRepository;
        this.chain = new ArrayList<>();
    }

    @PostConstruct
    public void init() {
        log.info("Initializing blockchain by loading existing blocks from database...");
        List<Block> loadedBlocks = blockRepository.findAll();
        // Sorting by timestamp to ensure correct chain order, as Mongo DB doesn't guarantee order.
        loadedBlocks.sort((b1, b2) -> {
            // A more robust sorting would ensure the chain structure itself is respected,
            // but for a simple load, timestamp is often sufficient if blocks are added sequentially.
            // For true chain reassembly, one might need to trace previousBlockHash.
            return b1.getTimestamp().compareTo(b2.getTimestamp());
        });
        this.chain.addAll(loadedBlocks);
        if (this.chain.isEmpty()) {
            log.warn("No blocks found in the database. The blockchain is empty. A genesis block will be created upon first transaction group.");
        } else {
            log.info("Loaded {} blocks. Latest block hash: {}", this.chain.size(), getLatestBlock().getCurrentBlockHash());
            // Optional run initial validation on start-up
            if (!validateChain()) {
                log.error("Blockchain integrity compromised on startup.");
                // Handle compromised chain, e.g,throw exception, trigger recovery
            } else {
                log.info("Blockchain integrity validated on startup.");
            }
        }
    }


    public Block addBlock(List<String> transactionIds) {
        if (transactionIds == null || transactionIds.isEmpty()) {
            throw new IllegalArgumentException("Cannot add an empty block. Provide transaction IDs");
        }

        log.info("Attempting to add new block with {} transactions...", transactionIds.size());

        // 1. Get transaction hashes from IDs (assuming transactionRepository can fetch them)
        // In a real scenario, you might pass hashes directly or fetch full transactions and hash them.
        List<String> transactionHashes = transactionIds.stream()
                .map(id -> transactionRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Transaction with ID " + id + "not found for block creation"))
                        .getSignature()) // Using signature as a unique hash-like identifier for Merkle tree.
                .collect(Collectors.toList());

        // 2. Build Merkle Tree and get Merkle Root
        merkleTreeBuilder.buildTree(transactionHashes);
        String merkleRoot = merkleTreeBuilder.getMerkleRoot();
        log.debug("Merkle Root calculated {}", merkleRoot);

        // 3. Determine previous block hash for chaining
        String previousBlockHash = "0"; // Default for the genesis block (first block)
        if (!chain.isEmpty()) {
            previousBlockHash = getLatestBlock().getCurrentBlockHash();
        } else {
            // Use the configured genesis hash for the very first block if chain is empty
            previousBlockHash = genesisBlockHash;
            log.info("Creating genesis block. Previous hash set to configured genesis hash: {}", genesisBlockHash);
        }

        // 4. Create and save new block
        Block newBlock = new Block(transactionIds, merkleRoot, previousBlockHash);
        newBlock = blockRepository.save(newBlock); // Save to DB to get its ID

        // 5. Add to in-memory chain
        this.chain.add(newBlock);
        log.info("New Block added to chain. ID: {}, Hash: {}, Tx Count: {}",
                newBlock.getId(), newBlock.getCurrentBlockHash(), newBlock.getTransactionsIds().size());

        return newBlock;
    }

    /**
     * Retrieves the latest block in blockchain.
     *
     * @return The latest Block, or null if the chain is empty.
     */
    public Block getLatestBlock() {
        if (chain.isEmpty()) {
            return null;
        }
        return chain.get(chain.size() - 1);
    }

    /**
     *
     */
    public boolean validateChain() {
        if (chain.isEmpty()) {
            log.warn("Attempted to validate empty chain. Returning true (trivially valid).");
            return true;
        }

        log.info("Validating blockchain integrity...");
        for (int i = 1; i < chain.size(); i++) {
            Block currentBlock = chain.get(i);
            Block previousBlock = chain.get(i - 1);

            // 1. Check if the current block's hash is correct
            if (!currentBlock.getCurrentBlockHash().equals(currentBlock.calculateHash())) {
                log.error("Block validation failed: Block {} has an invalid current hash. Expected: {}, Got: {}",
                        currentBlock.getId(), currentBlock.calculateHash(), currentBlock.getCurrentBlockHash());
                return false;
            }

            // 2. Check if the current block's previous hash points to the actual previous block's hash
            if (!currentBlock.getPreviousBlockHash().equals(previousBlock.getCurrentBlockHash())) {
                log.error("Block validation failed: Block {} previous has mismatch. Points to: {}, Actual previous: {}",
                        currentBlock.getId(), currentBlock.calculateHash(), previousBlock.getCurrentBlockHash());
                return false;
            }

            // 3. Verify Merkle Root Consistency
            // Fetch transaction hashes from DB using transactionIds in currentBlock
            List<String> currentBlockTxHashes = currentBlock.getTransactionsIds().stream()
                    .map(id -> transactionRepository.findById(id)
                            .orElseThrow(() -> new RuntimeException("Transaction with ID " + id + "not found during chain validation."))
                            .getSignature())
                    .collect(Collectors.toList());

            merkleTreeBuilder.buildTree(currentBlockTxHashes);
            String calculatedMerkleRoot = merkleTreeBuilder.getMerkleRoot();

            if (!currentBlock.getMerkleRoot().equals(calculatedMerkleRoot)) {
                log.error("Block validation failed: Block{} Merkle Root mismatch. Expected: {}, Got: {}",
                        currentBlock.getId(), calculatedMerkleRoot, currentBlock.getMerkleRoot());
                return false;
            }
        }

        log.info("Blockchain validation successful");
        return true;
    }
}
