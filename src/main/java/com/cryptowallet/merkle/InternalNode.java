package com.cryptowallet.merkle;

/**
 * Represents an internal node in a Merkle Tree (Composite Pattern - Composite).
 * Its hash is calculated by hashing the concatenated hashes of its left and right children.
 */
public class InternalNode extends MerkleNode {
    private final MerkleNode left;
    private final MerkleNode right;

    /**
     * Constructs an InternalNode with two child nodes.
     * The hash of this node is computed by hashing the concatenation of its children's hashes.
     * @param left The left child MerkleNode.
     * @param right The right child MerkleNode.
     */
    public InternalNode(MerkleNode left, MerkleNode right) {
        this.left = left;
        this.right = right;
        // The hash of an internal node is the hash of the concatenation of its children's hashes
        this.hash = calculateSha256Hash(left.getHash() + right.getHash());
    }

    /**
     * Returns the pre-calculated hash of this internal node.
     * @return The SHA-256 hash of the concatenated children hashes.
     */
    @Override
    public String getHash() {
        return hash;
    }

    public MerkleNode getLeft() {
        return left;
    }

    public MerkleNode getRight() {
        return right;
    }
}
