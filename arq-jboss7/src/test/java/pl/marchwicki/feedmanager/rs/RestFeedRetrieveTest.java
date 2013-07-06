package pl.marchwicki.feedmanager.rs;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.webapp30.WebAppDescriptor;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;
import org.junit.runner.RunWith;

import pl.marchwicki.feedmanager.FeedsRepository;
import pl.marchwicki.feedmanager.FeedsService;
import pl.marchwicki.feedmanager.InMemoryFeedsRepository;
import pl.marchwicki.feedmanager.log.FeedBodyLoggingInterceptor;
import pl.marchwicki.feedmanager.log.FeedEventLog;
import pl.marchwicki.feedmanager.model.Feed;
import pl.marchwicki.feedmanager.model.FeedBuilder;
import pl.marchwicki.feedmanager.model.Item;

@RunWith(Arquillian.class)
public class RestFeedRetrieveTest {

	private final String FEED_NAME = "javalobby";
	
	@Deployment(testable = false)
	public static WebArchive createDeployment() throws Exception {
		File[] libs = Maven.resolver().loadPomFromFile("pom.xml")
				.resolve("rome:rome:0.9").withTransitivity()
				.asFile();

		WebAppDescriptor web = Descriptors.create(WebAppDescriptor.class)
				.version("3.0")
				.createServletMapping()
				.servletName("javax.ws.rs.core.Application").urlPattern("/*")
				.up();
		
		return ShrinkWrap
				.create(WebArchive.class, "test.war")
				.addAsLibraries(libs)
				.addClass(RestFeedConsumerEndpoint.class)
				.addClasses(FeedsService.class, FeedBuilder.class, FeedsRepository.class)
				.addClasses(Feed.class, Item.class)
				.addClass(InMemoryFeedsRepository.class)
				
				//remove those to see how meaningless exceptions you get upon deployment
				.addClasses(FeedEventLog.class, FeedBodyLoggingInterceptor.class)
				.addAsWebInfResource(EmptyAsset.INSTANCE,
						ArchivePaths.create("beans.xml"))
				.setWebXML(new StringAsset(web.exportAsString()));
	}

	@Test
	@RunAsClient
	public void shouldReturnNotFoundForNoFeedsTest(@ArquillianResource URL baseURL) throws Exception {
		//given
		DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpGet get = new HttpGet(baseURL.toURI() + "rs/feed/"+FEED_NAME);
		
		//when
		HttpResponse response = httpclient.execute(get);
		
		//then
		assertThat(response.getStatusLine().getStatusCode(), equalTo(404));
	}
	
}