package com.sandbox.server.mcp;

import com.sandbox.server.demo.client.PostsWebClient;
import com.sandbox.server.demo.dto.PostDto;
import com.sandbox.server.demo.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RestTools {

    private final FileService fileService;
    private final PostsWebClient postsWebClient;

    @Tool(description = "Fetches the list of posts from an external service")
    public List<PostDto> posts() {
        return postsWebClient.getAllPosts();
    }

    @Tool(description = "Fetches the passphrase from an external service")
    public String passphrase() {
        return postsWebClient.getPassphrase();
    }

    @Tool(description = "Saves the given content to a file on the server")
    public void saveFile(
            @ToolParam(description = "Content to write to the file") String content) {
        fileService.save(content);
    }
}
