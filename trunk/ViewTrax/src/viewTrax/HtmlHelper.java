package viewTrax;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import viewTrax.SingletonWrapper.CacheDatePair;
import viewTrax.data.Title;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Dictionary;
import java.util.List;
import java.util.Stack;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;


public class HtmlHelper {
	private static final Logger	log	= Logger.getLogger( HtmlHelper.class.getName() );

	private static void LogInfo( String queryName, String msg ) {
		log.info( "[Query:" + queryName + "] " + msg );
	}

	private static void addAttribute( StringBuilder sb, String name,
			String value ) {
		sb.append( name ).append( "='" ).append( value ).append( "' " );
	}

	public static String createTagA( String url, String content ) {
		StringBuilder sb = new StringBuilder( "<a " );
		addAttribute( sb, "href", url );
		sb.append( ">" ).append( content ).append( "</a>" );
		return sb.toString();
	}

	public static String createTagA( String url, String id, String content ) {
		StringBuilder sb = new StringBuilder( "<a " );
		addAttribute( sb, "href", url );
		addAttribute( sb, "id", id );
		sb.append( ">" ).append( content ).append( "</a>" );
		return sb.toString();
	}

	public static <T> String surroundWithQuotes( T value ) {
		return "'" + value.toString() + "'";
	}

	/**
	 * Goes to the page stored in {@link #detailsPage} to obtain detailed
	 * information about the title.
	 * 
	 * @return Raw {@link Node} array of the overview/description of the title
	 */
	public static List<Node> getWikiDetails( Title title ) {
		final String url = title.getDetailsPage().getValue();

		// First check if cache is valid
		Dictionary<String, CacheDatePair<List<Node>>> cache = SingletonWrapper.get().getTitleDetailsCache();
		CacheDatePair<List<Node>> cacheDatePair = cache.get( url );
		if( cacheDatePair != null && cacheDatePair.date.after( new Date() ) ) {
			return (List<Node>) cacheDatePair.cache;
		}

		// Cache is invalid
		Stack<Node> details = new Stack<Node>();
		try {
			DocumentBuilderFactory docBuilder = DocumentBuilderFactory.newInstance();
			Document doc = docBuilder.newDocumentBuilder().parse( url );
			// HACK use wiki only for now
			// HACK Get everything before the ToC element
			Element toc = doc.getElementById( "toc" );

			Node summary = toc.getPreviousSibling();
			// HACK we don't want anything that's not the summary
			while( !summary.getNodeName().contains( "table" ) ) {
				if( summary.getNodeName().equals( "p" ) ) {
					details.push( summary );
				}
				summary = summary.getPreviousSibling();
			}
		} catch( Exception e ) {
			// TODO log error
			LogInfo( "getWikiDetails", "Failed to parse wiki page" );
		}

		// We need to reverse this list
		List<Node> list = new ArrayList<Node>();
		while( !details.isEmpty() ) {
			list.add( details.pop() );
		}
		list = Collections.unmodifiableList( list ); // Defensive copying

		final int cacheValidity = 5;
		Calendar cal = Calendar.getInstance();
		cal.add( Calendar.DATE, cacheValidity );
		Date validUntil = cal.getTime();

		// Update cache before returning
		cache.put( url, new CacheDatePair<List<Node>>( list, validUntil ) );
		return list;
	}

	/**
	 * Goes to the page stored in {@link #detailsPage} to obtain detailed
	 * information about the title.
	 * 
	 * @return {@link String} array of the overview/description of the title
	 */
	public static String[] getDetails( Title title ) {
		List<Node> wikiDetails = getWikiDetails( title );
		String[] details = new String[wikiDetails.size()];


		class SimpleWebFilter implements WebFilter {
			@Override
			public String getContext( Node node ) {
				return node.getTextContent();
			}
		}
		WebFilter filter = new WikiLinksWebFilter();// SimpleWebFilter();

		for( int i = 0; i < details.length; i++ ) {
			details[i] = filter.getContext( wikiDetails.get( i ) );
		}
		return details;
	}

	public interface WebFilter {
		public String getContext( Node node );
	}

	public static class WikiLinksWebFilter implements WebFilter {
		@Override
		public String getContext( Node root ) {
			StringBuilder sb = new StringBuilder();
			NodeList childNodes = root.getChildNodes();

			String namespace = root.getOwnerDocument().getDocumentURI();
			namespace = namespace.substring( 0, namespace.indexOf( "/wiki" ) );

			for( int i = 0; i < childNodes.getLength(); i++ ) {
				Node node = childNodes.item( i );
				sb.append( ' ' );
				if( node.getNodeName() == "a" ) {
					appendNodeRawHtml( sb, node );
//					String href = node.getAttributes().getNamedItem( "href" ).getTextContent();
//					sb.append( "<a href='" ).append( namespace ).append( href ).append(
//							"'>" );
//					sb.append( node.getTextContent() ).append( "</a> " );
				} else {
					sb.append( node.getTextContent() );
				}

			}
			return sb.toString();
		}
	}

	public static void appendNodeRawHtml( StringBuilder sb, Node node ) {

		String namespace = node.getOwnerDocument().getDocumentURI();
		namespace = namespace.substring( 0, namespace.indexOf( "/wiki" ) );

		// Open tag
		sb.append( "<" ).append( node.getNodeName() );

		NamedNodeMap attrNodes = node.getAttributes();
		for( int i = 0; i < attrNodes.getLength(); i++ ) {
			Node attr = attrNodes.item( i );
			sb.append( ' ' ).append( attr.getNodeName() ).append( "=\"" );
			if( attr.getNodeName().equals( "href" )) {
				sb.append( namespace );
			}
			sb.append( attr.getTextContent() ).append( '\"' );
		}
		
		// close the tag
		sb.append( " >" ).append( node.getTextContent() ).append( "</a> " );
	}

}
