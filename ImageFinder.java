package com.eulerity.hackathon.imagefinder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import java.util.LinkedList;
import java.util.Set;
import java.util.Queue;
import java.util.ArrayList;
import java.util.List;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.HashSet;
import java.net.URISyntaxException;
import java.net.URI;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.stream.IntStream;

//Jessica Saviano
@WebServlet(
		name = "ImageFinder",
		urlPatterns = {"/main"}
)
public class ImageFinder extends HttpServlet{
	private static final long serialVersionUID = 1L;
	protected static final Gson GSON = new GsonBuilder().create();
	private static final int MAX_DEPTH = 2;
	Queue<String> queue = new LinkedList<>();
	Set<String> visited = new HashSet<>();
	int workingThreads = 0;  // new code

	//method that will crawl the images from the url and the other scannable url's on the page.
	public ArrayList<String> getImages(String URL) {
		ArrayList<String> all_images = new ArrayList<String>();
		try {

			Document doc = Jsoup.connect(URL).get();
			Elements images = doc.select("img[src]");

			for (Element i : images) {
				all_images.add(i.attr("src"));

			}
		} catch (IOException e) {
			System.err.println("For '" + URL + "': " + e.getMessage());
		}

		return all_images;

	}

	//this method is what takes the longest/needs optimization because it needs to go through the entire url and find all possible links on the page.
	public ArrayList<String> getURLs(String URL) {
		ArrayList<String> all_links = new ArrayList<String>();
		if(URL.charAt(URL.length() - 1) == '/'){
			URL = URL.substring(0, URL.length()-1);
		}
		try {
			URI uri = new URI(URL);
			String domain = uri.getHost();

			try {
				System.out.println(URL);
				Document doc = Jsoup.connect(URL).get();
				Elements links = doc.select("a[href]");

				for (Element l : links) {
					URI uri_test = new URI(l.attr("href"));
					String link = l.attr("href");
					if (link.length() == 0) break; //making sure it's an actual link
					if (link.charAt(0) == '#') continue; //making sure it's an actual link

					// if link is relative
					if (link.charAt(0) == '/' && uri_test.getHost() != null) {
						// add base URL to link, add to all_links
						all_links.add("http://" + uri_test.getHost() + link);

					} else if (uri_test.getHost() != null && uri_test.getHost().equals(domain)) { // absolute link, check if link is in domain
						all_links.add(link);
					}
				}
			}

			catch (IOException e) {
				System.err.println("For '" + URL + "': " + e.getMessage());
			}
		}
		catch (URISyntaxException e) {
			System.err.println(URL);
		}
		return all_links;

	}
	//Method to find all accessible links/URls. This is done with a simple BFS. In order to optimize the code I made the scan multihthreaded and synscornzied the threads.
	public Set<String> findAll() {
		Queue<String> queue_data = new LinkedList<>();
		Set<String> set_data = new HashSet<>();
		int count = 0;
		int threads = 0;
		outer: while(true) {
			String next_url;
			//first part of multithreading
			synchronized(this) {
				while(queue_data.isEmpty()) {
					if(queue_data.isEmpty() && threads == 0){
						break outer;
					}
					try {
						wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				next_url = queue.poll();
				threads++;
			}
			//second part of multithreading. call to getUrls that takes up the most time.

			List<String> URLs = getURLs(next_url);
			//third part of multithreading
			synchronized(this) {
				//BFS to find next scannable url
				for(String newUrl: URLs) {
					if(!set_data.contains(newUrl)) {
						queue_data.offer(newUrl);
						if(count > 50) break; //set a depth
						set_data.add(newUrl);
						count++;
					}
				}
				threads--;
				notifyAll();
			}
		}
		//returns a set of scannable urls.
		return set_data;
	}


	@Override
	protected final void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("text/json");
		String path = req.getServletPath();
		String url = req.getParameter("url");
		System.out.println("Got request of:" + path + " with query param:" + url);

		ArrayList<String> one_url_images = new ArrayList<String>();
		ArrayList<String> final_image_list = new ArrayList<String>();
		Set<String> final_set = new HashSet<>();

		one_url_images = getURLs(url);
		final_set = findAll();
		for(String image : one_url_images) {
			final_image_list.addAll(getImages(image));

		}
		//put the data in the correct type to then be returned/printed on the website!
		String[] finalized = new String[final_image_list.size()];
		int count = 0;
		for(String i :final_image_list){
			finalized[count] = i;
			count+=1;
		}
		resp.getWriter().print(GSON.toJson(finalized));
	}


}
