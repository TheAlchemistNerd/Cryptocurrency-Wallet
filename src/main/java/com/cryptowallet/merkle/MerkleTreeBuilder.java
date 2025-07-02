package com.cryptowallet.merkle;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Builds a Merkle Tree from a list of transaction hashes and computes the Merkle Root.
 * This component is responsible for creating the tree structure based on the Composite Pattern MerkleNodes.
 */
public class MerkleTreeBuilder {
    private MerkleNode root;

    /**
     * Constructs the Merkle Tree from a list of transaction hashes.
     * The method handles both even and odd numbers of leaf nodes by duplicating the last hash if necessary.
     * @param transactionHashes A list of SHA-256 hashes of individual transactions.
     * @return The root node of the constructed Merkle Tree.
     * @throws IllegalArgumentException if the list of transaction hashes is empty or null.
     */
    public MerkleNode buildTree(List<String> transactionHashes) {
        if(transactionHashes == null || transactionHashes.isEmpty()) {
            throw new IllegalArgumentException("Transaction hashes list cannot be empty or null to build a Merkle Tree.");
        }

        // Convert transaction hashes into LeafNodes
        List<MerkleNode> currentLevel = transactionHashes.stream()
                .map(LeafNode::new)
                .collect(Collectors.toList());

        // Recursively build up the tree until only one node (the root) remains
        while (currentLevel.size() > 1) {
            List<MerkleNode> nextLevel = new ArrayList<>();

            // Handle odd number of nodes by duplicating the last one
            if (currentLevel.size() % 2 != 0) {
                currentLevel.add(currentLevel.get(currentLevel.size() - 1));
            }

            // Combine pairs of nodes into InternalNodes
            for (int i = 0; i < currentLevel.size(); i += 2) {
                MerkleNode left = currentLevel.get(i);
                MerkleNode right = currentLevel.get(i + 1);
                nextLevel.add(new InternalNode(left, right));
            }
            currentLevel = nextLevel;
        }

        this.root = currentLevel.get(0);
        return this.root;
    }

    /**
     * Returns the Merkle Root (the hash of the root node) of the built tree.
     * Must be called after buildTree().
     * @return The Merkle Root as a Base64 encoded SHA-256 hash.
     * @throws IllegalStateException if buildTree() has not been called yet.
     */
    public String getMerkleRoot() {
        if(this.root == null) {
            throw new IllegalStateException("Merkle tree has not been built yet. Call buildTree() first.");
        }
        return this.root.getHash();
    }
}
