package cc.dodder.common.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;

import java.util.Date;

@Getter @Setter
public class WebConfig {

    @Id
    private String id;

    private String adminUsername;
    private String adminPassword;

    private Date startTime;

}
