package recipeagent;

import java.util.List;

public interface LLMClient {
    String sendMessage(String systemPrompt, List<ChatMessage> messages);
}
