package de.wps.dot.filmprogramm;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.TimerTask;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.agent.model.NewCheck;
import com.ecwid.consul.v1.agent.model.NewService;

public class MicroFilmprogrammService {

	public static void main(String[] args) {
		

		if (args.length < 1) {
			System.out.println("missing command-line argument: port");
			return;
		}

		int port = Integer.parseInt(args[0]);

		new MicroFilmprogrammService().start(port);
	}

	private List<Vorführung> ladeVorführungen() {
		return Arrays.asList(//
				new Vorführung("Filmy McFilmface", "grosser_saal", LocalDateTime.now().withHour(14).withMinute(0)), //
				new Vorführung("Filmy McFilmface", "kleiner_saal", LocalDateTime.now().withHour(16).withMinute(0)), //
				new Vorführung("Filmy McFilmface", "kleiner_saal", LocalDateTime.now().withHour(19).withMinute(0)));
	}

	private WebFilmprogrammService service;

	public MicroFilmprogrammService() {
		service = new WebFilmprogrammService(ladeVorführungen());
	}

	public void start(int port) {
		service.start(port);
		// Umlaute werden beim DNS nicht unterstützt
		
		NewService newService = new NewService();
		newService.setName("Filmprogramm");
		String serviceId = "filmprogramm-" + port;
		newService.setId(serviceId);
		newService.setPort(port);
		
		NewService.Check newCheck = new NewService.Check();
		newCheck.setTtl("10s");
		newService.setCheck(newCheck);
		
		NewCheck httpCheck = new NewCheck();
		httpCheck.setHttp("http://localhost:" + port + "/vorführungen");
		httpCheck.setName("FilmprogrammHttp");
		httpCheck.setId("filmprogrammHttp-" + port);
		httpCheck.setTimeout("2s");
		httpCheck.setInterval("10s");
		
		
		ConsulClient consul = new ConsulClient();		
		consul.agentCheckRegister(httpCheck);
		consul.agentCheckDeregister("FilmprogrammHttp");
		consul.agentCheckDeregister("FilmprogrammTTL");
		consul.agentServiceRegister(newService);
		
		startePeriodischenHeartbeat("service:filmprogramm-"+port);
	}

	private void startePeriodischenHeartbeat(String checkId) {
		new TimerTask() {
			
			@Override
			public void run() {
				while(true){
					try {
						ConsulClient client = new ConsulClient();
						client.agentCheckPass(checkId);
						Thread.sleep(3000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				
			}
		}.run();;
		
	}

	public void stop() {
		service.stop();
	}

}