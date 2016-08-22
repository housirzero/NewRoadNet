package team.trans.zone;

import java.awt.Polygon;
import java.util.ArrayList;

import team.net.graph.LngLat;

/**
 *  交通小区
 */
public class TransZone
{

	public int id;
	/**
	 * 质心
	 */
	public LngLat center;
	public ArrayList<LngLat> pointsList;
	
	public TransZone(int zoneId)
	{
		this.id = zoneId;
		this.pointsList = new ArrayList<LngLat>();
	}

	public TransZone(int zoneId, LngLat lngLat)
	{
		this.id = zoneId;
		this.center = lngLat.Copy();
		this.pointsList = new ArrayList<LngLat>();
	}

	/**
	 *  Determines whether the point is inside this 
     * <code>TransZone</code>.   
     * <p>
	 */
	public boolean contains( LngLat point )
	{
		if(pointsList != null && pointsList.size() > 0)
		{
			int[] xpoints = new int[pointsList.size()];
			int[] ypoints = new int[pointsList.size()];
			for( int i = 0; i < pointsList.size(); ++i )
			{
				xpoints[i] = (int)(pointsList.get(i).lat * 10000000);
				ypoints[i] = (int)(pointsList.get(i).lng * 10000000);
			}
			Polygon polygon = new Polygon(xpoints, ypoints, pointsList.size());
			return polygon.contains(point.lat * 10000000, point.lng* 10000000);
		}
		return false;
	}
	
	/***
	 * 深拷贝
	 */
	public TransZone deepCopy()
	{
		return null;
	}
	
}
