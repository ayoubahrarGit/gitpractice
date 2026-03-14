"""
What LLM (Large Language Model) Code Looks Like
================================================

This file demonstrates the core building blocks of a Large Language Model:
1. Tokenization  - Converting text into numbers
2. Embeddings    - Representing tokens as vectors
3. Self-Attention - The key mechanism behind transformers
4. Transformer   - The architecture used by GPT, LLaMA, etc.
5. Text Generation - Producing text token by token

This is a simplified, educational implementation. Production LLMs like
GPT-4 or LLaMA use the same concepts at a much larger scale.
"""

import math


# ---------------------------------------------------------------------------
# 1. TOKENIZER – converts text to token IDs and back
# ---------------------------------------------------------------------------
class SimpleTokenizer:
    """A character-level tokenizer (real LLMs use sub-word tokenizers like BPE)."""

    def __init__(self, text):
        chars = sorted(set(text))
        self.char_to_id = {ch: i for i, ch in enumerate(chars)}
        self.id_to_char = {i: ch for i, ch in enumerate(chars)}
        self.vocab_size = len(chars)

    def encode(self, text):
        """Convert a string to a list of token IDs."""
        return [self.char_to_id[ch] for ch in text if ch in self.char_to_id]

    def decode(self, ids):
        """Convert a list of token IDs back to a string."""
        return "".join(self.id_to_char[i] for i in ids if i in self.id_to_char)


# ---------------------------------------------------------------------------
# 2. MATH HELPERS – pure-Python versions of operations normally done by
#    frameworks like PyTorch or TensorFlow
# ---------------------------------------------------------------------------
def dot(a, b):
    """Dot product of two vectors."""
    return sum(x * y for x, y in zip(a, b))


def matmul(A, B):
    """Multiply two 2-D matrices (lists of lists)."""
    rows_a, cols_b = len(A), len(B[0])
    cols_a = len(A[0])
    result = [[0.0] * cols_b for _ in range(rows_a)]
    for i in range(rows_a):
        for j in range(cols_b):
            for k in range(cols_a):
                result[i][j] += A[i][k] * B[k][j]
    return result


def transpose(M):
    """Transpose a 2-D matrix."""
    return list(map(list, zip(*M)))


def softmax(values):
    """Softmax converts raw scores into probabilities."""
    max_val = max(values)
    exps = [math.exp(v - max_val) for v in values]
    total = sum(exps)
    return [e / total for e in exps]


# ---------------------------------------------------------------------------
# 3. SELF-ATTENTION – the heart of every transformer
# ---------------------------------------------------------------------------
def scaled_dot_product_attention(Q, K, V):
    """
    Scaled dot-product attention (single head).

    Q, K, V are 2-D matrices where each row is a token vector.
    Returns the attention output matrix.

    In a real LLM this runs on GPUs across many "heads" in parallel.
    """
    d_k = len(K[0])  # dimension of key vectors
    # Score = Q · Kᵀ / √d_k
    KT = transpose(K)
    scores = matmul(Q, KT)
    scale = math.sqrt(d_k)
    scores = [[val / scale for val in row] for row in scores]
    # Apply softmax row-wise to get attention weights
    weights = [softmax(row) for row in scores]
    # Output = weights · V
    output = matmul(weights, V)
    return output


# ---------------------------------------------------------------------------
# 4. TRANSFORMER BLOCK (simplified)
# ---------------------------------------------------------------------------
class MiniTransformerBlock:
    """
    A single transformer block with self-attention and a feed-forward layer.

    Real LLMs stack dozens (or hundreds) of these blocks on top of each other.
    """

    def __init__(self, d_model):
        self.d_model = d_model

    def forward(self, X):
        """
        X: list of token embedding vectors (seq_len × d_model).
        Returns transformed embeddings of the same shape.
        """
        # Self-attention (Q = K = V = X for self-attention)
        attn_out = scaled_dot_product_attention(X, X, X)
        # Residual connection  (skip connection)
        out = [
            [x + a for x, a in zip(x_row, a_row)]
            for x_row, a_row in zip(X, attn_out)
        ]
        return out


# ---------------------------------------------------------------------------
# 5. TEXT GENERATION – sampling the next token
# ---------------------------------------------------------------------------
def generate_next_token_probabilities(logits):
    """
    Convert raw model output (logits) for the last position into a
    probability distribution over the vocabulary.
    """
    return softmax(logits)


# ---------------------------------------------------------------------------
# DEMO – putting it all together
# ---------------------------------------------------------------------------
def main():
    # --- Tokenize some text ---
    corpus = "hello world"
    tokenizer = SimpleTokenizer(corpus)
    token_ids = tokenizer.encode(corpus)
    print("=== Tokenization ===")
    print(f"Text:       {corpus!r}")
    print(f"Token IDs:  {token_ids}")
    print(f"Decoded:    {tokenizer.decode(token_ids)!r}")
    print(f"Vocab size: {tokenizer.vocab_size}")

    # --- Create tiny embeddings (normally learned during training) ---
    d_model = 4  # embedding dimension (real LLMs use 4096+)
    embeddings = [
        [float((tid * (d + 1) + 1) % 7) / 7.0 for d in range(d_model)]
        for tid in token_ids
    ]
    print("\n=== Embeddings (first 3 tokens) ===")
    for i, emb in enumerate(embeddings[:3]):
        print(f"  Token {token_ids[i]} -> {[round(v, 3) for v in emb]}")

    # --- Run a transformer block ---
    block = MiniTransformerBlock(d_model)
    output = block.forward(embeddings)
    print("\n=== After Transformer Block (first 3 tokens) ===")
    for i, vec in enumerate(output[:3]):
        print(f"  Token {token_ids[i]} -> {[round(v, 3) for v in vec]}")

    # --- Simulate next-token prediction ---
    last_hidden = output[-1]
    probs = generate_next_token_probabilities(last_hidden)
    print("\n=== Next-Token Probabilities ===")
    for tid, p in enumerate(probs):
        char = tokenizer.id_to_char.get(tid, "?")
        print(f"  '{char}' (id {tid}): {p:.4f}")

    print("\nDone! This is the basic flow inside every LLM.")


if __name__ == "__main__":
    main()
