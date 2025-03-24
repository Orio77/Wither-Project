package com.Orio.wither_project.socket.gather.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QAProgressDTO {
    private String question;
    private String answer;
}
