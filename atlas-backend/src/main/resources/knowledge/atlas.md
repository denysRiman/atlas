# Atlas Knowledge Assistant

Atlas is an internal AI knowledge assistant for enterprise teams.

Atlas can answer questions using company documents, policies, and internal technical documentation. The assistant uses retrieval-augmented generation to find relevant document fragments before generating an answer.

Atlas supports multi-turn conversations and separates conversation history by conversation ID. It can also call external tools when information must be retrieved dynamically.

The first version of Atlas uses Amazon Bedrock for model inference and embeddings. Amazon Nova Micro is used for conversational responses. Titan Text Embeddings V2 is used to generate vector representations of document chunks.

Atlas should prefer information retrieved from the knowledge base over unsupported assumptions. If the retrieved documents do not contain enough information, Atlas should clearly state that the answer cannot be determined from the available knowledge.