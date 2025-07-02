package com.cryptowallet.merkle;

public class LeafNode extends MerkleNode {
    /**
     * Constructs a LeafNode with given transaction hash.
     * The hash of this node is simply the provided transaction hash.
     * @param transactionHash the hash of the transaction represented by this leaf.
     */
    public LeafNode(String transactionHash) {
        this.hash = transactionHash;
    }

    /**
     * Returns the hash of this leaf node, which is the associated transaction hash.
     * @return The transaction hash.
     */
    @Override
    public String getHash() {
        return hash;
    }

}
