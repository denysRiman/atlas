# Manual RAG Retrieval Evaluation

This document describes the retrieval evaluation and parameter tuning performed
for the first manual RAG implementation in Atlas.

## Goal

The goal was to select reasonable retrieval parameters based on measured
retrieval behavior rather than arbitrary defaults.

The following parameters were evaluated:

- chunk size
- chunk overlap
- top-K retrieval
- minimum similarity threshold

Amazon Titan Text Embeddings V2 with normalized 1024-dimensional embeddings
and pgvector cosine similarity were used for all experiments.

## Evaluation Dataset

A small fixed evaluation set of nine questions was used.

The set contains:

- exact factual questions
- paraphrased factual questions
- semantic retrieval questions
- questions that cannot be answered from the knowledge base

Examples include:

1. How much does the company reimburse for hotels?
2. What is the maximum hotel reimbursement per night?
3. Which model does Atlas use for text generation?
4. Which model is used to generate embeddings?
5. What technologies does Atlas Labs use for backend development?
6. Where is Atlas Labs based?
7. What should Atlas do when retrieved knowledge is insufficient?
8. What is the CEO's phone number?
9. What is the company car reimbursement rate?

The last two questions are intentionally unsupported by the knowledge base and
are used to observe false-positive retrieval behavior.

During evaluation, the similarity threshold was temporarily disabled so that
raw retrieval results and scores could be inspected.

## 1. Chunk Size Evaluation

Three configurations were compared:

| Chunk size | Overlap |
|---:|---:|
| 400 | 50 |
| 250 | 30 |
| 650 | 80 |

The 250-character chunks generally produced more focused embeddings for
specific factual questions.

For example, the hotel reimbursement question improved from approximately
0.50 with 400-character chunks to approximately 0.68 with 250-character
chunks.

The embedding-model question improved from approximately 0.29 to 0.35.

Large 650-character chunks performed worse on several specific factual
questions because more unrelated context was included in a single embedding.

Selected value:

`chunkSize = 250`

## 2. Chunk Overlap Evaluation

After selecting a chunk size of 250, three overlap values were compared:

| Chunk size | Overlap |
|---:|---:|
| 250 | 0 |
| 250 | 30 |
| 250 | 60 |

An overlap of 30 produced the best overall balance.

No overlap reduced retrieval quality for facts located close to chunk
boundaries.

An overlap of 60 introduced more duplicated context and increased similarity
for some irrelevant results.

For example, the unsupported company-car question reached a similarity score
of approximately 0.33 with overlap 60, making threshold separation worse.

Selected value:

`overlap = 30`

## 3. Top-K Evaluation

All positive questions in the evaluation set retrieved the expected information
at rank 1 with the selected chunk configuration.

A separate large Top-K sweep was therefore not necessary for the current small
knowledge base.

`topK = 3` was retained instead of reducing it to 1 because future questions
may require information from multiple chunks.

This provides additional context while keeping the amount of retrieved data
small.

Selected value:

`topK = 3`

## 4. Similarity Threshold Evaluation

The threshold was evaluated using raw retrieval scores from both supported and
unsupported questions.

With the selected chunk configuration (`250 / 30`), the important boundary was
approximately:

- lowest relevant top result: `0.349`
- highest irrelevant top result: `0.264`

This created a useful separation between relevant and irrelevant retrieval:

```text
irrelevant ≈ 0.264

-------- threshold 0.30 --------

relevant   ≈ 0.349

```

A threshold of 0.30 was therefore selected.
This value is specific to the current embedding model, knowledge base and
evaluation dataset. It should be re-evaluated when these change.

## Final Configuration

```text
atlas.chunk.size=250
atlas.chunk.overlap=30
atlas.knowledge.retrieval.top-k=3
atlas.knowledge.retrieval.min-similarity=0.30
```

## Result

The final configuration was selected based on retrieval measurements rather
than arbitrary parameter values.
The evaluation also demonstrated several limitations of fixed-size
character-based chunking, especially chunks beginning or ending in the middle
of words or semantic sections.
These results provide a baseline for the next Atlas RAG stage, where the manual
implementation will be compared with Amazon Bedrock Knowledge Bases and
different managed chunking strategies.