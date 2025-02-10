package com.raphaelprojetos.sentinel;

import com.raphaelprojetos.sentinel.tray.SwingManager;
import com.raphaelprojetos.sentinel.tray.TrayManager;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class SentinelApplication {
	public static void main(String[] args) {
		ConfigurableApplicationContext context =
				new SpringApplicationBuilder(SentinelApplication.class)
						.headless(false)
						.web(WebApplicationType.NONE)
						.run(args);

		SwingManager swingManager = context.getBean(SwingManager.class);
		TrayManager trayManager = new TrayManager();
		trayManager.initTray();
	}
}