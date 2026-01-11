package dto;

import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class GenTokenRequest {
    private Long userId;
    private String username;
    private List<Integer> roles;
    private Set<String> permissions;
}
