package snapshot;

import java.io.BufferedReader;

import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import data.feature.CategoricalFeature;
import data.feature.Feature;
import data.feature.KeyFeature;
import data.feature.NumericFeature;
import data.feature.SpatialFeature;


public class SnapshotSchema implements Serializable, Cloneable{
	
	private KeyFeature key;
	private List<SpatialFeature> spatial=new ArrayList<SpatialFeature>();
	private List<Feature> target=new ArrayList<Feature>();
	
	public SnapshotSchema (String iniFile) throws IOException,ErrorFormatException{
		FileReader inputFileReader   = new FileReader(iniFile);        
        BufferedReader inputStream   = new BufferedReader(inputFileReader);
        String inline=inputStream.readLine();
      

        int idStream=0;
        while(inline!=null)
        {
        	if(!inline.startsWith("@", 0) || inline.isEmpty()) //escape line
        	{
        		inline=inputStream.readLine();
        		continue;
        	}
        		
        	
        		String[]str=inline.split(" ");
        		String code="";
        		String name="";
        		for(int i=0;i<str.length;i++)
        		{
        			if(str[i].isEmpty())
        				continue;
        			if(code.isEmpty()){
        				code= str[i].replace("@", "").toLowerCase();
        				
        				continue;
        			}
        			if(name.isEmpty()){
        				name=str[i].toLowerCase();
        				break;
        			}
        		
        		}
        	
        		if(code.isEmpty() || name.isEmpty())
        			throw new ErrorFormatException(inline);
        		if(code.equals("key")){
        			key=new KeyFeature(name,idStream);
        			idStream++;
        			inline=inputStream.readLine();
        			continue;
        		}
        		else if(code.equals("spatial")){
        			spatial.add( new SpatialFeature(name,idStream));
        			
        		}
        		else if(code.equals("numeric")){
        			target.add(new NumericFeature(name,idStream));
        			
        		}
        		else if(code.equals("categorical")){
        			target.add(new CategoricalFeature(name,idStream));
        			
        		}
        		else if(code.equals("ignored"))
        		{
        			//System.out.println(name+":ignored");
        			idStream++;
        			inline=inputStream.readLine();
        			continue;
        		}
        		else
        		{
        			System.out.println(name+":unknown");
        			inline=inputStream.readLine();
        			continue;
        		}
        		
        		idStream++;
        			
        	
        	inline=inputStream.readLine();
        }
        
        if(spatial.isEmpty())
        	throw new ErrorFormatException("No spatial feature in the stream");
        if(target.isEmpty())
        	throw new ErrorFormatException("No target feature in the stream");
       // if(key==null)
        //	throw new ErrorFormatException("No sensor key in the stream");
        
        //Update mining index (TRANNE DI) key, spatial, spatial,..., spatial, target, target, ..., target
        int id=0;
        for(SpatialFeature fs:spatial){
        	fs.setIndexMining(id++);
        }
        for(Feature f:target){
        	f.setIndexMining(id++);
        }
        inputStream.close();
        inputFileReader.close();
        
	}
	
	public KeyFeature getKey(){
		return key;
	}
	
	public List<SpatialFeature> getSpatialList(){
		return spatial;
	}
	
	public List<Feature> getTargetList(){
		return target;
	}
	
	public List<Feature> getTargetListNotLeaf(){
		List<Feature> l=new ArrayList<Feature>();
		for(Feature f:target)
			if(!f.getStopTree())
				l.add(f);
		return l;
	}
	
	public void reset(){
		for(SpatialFeature fs:spatial){
			fs.clear();
		}
		
		for(Feature fs:target){
			if(!fs.getStopTree()) // altrimenti Mantengo l'ultimo modello costruito
				fs.clear();
			
				
		}
		
	}

	public String toString(){
		return "key"+key+"\n"+"spatial attributes:\n"+spatial+"\n"+"target attributes\n"+target;
	}

	public Object clone(){
		
		try {
			SnapshotSchema s= (SnapshotSchema)super.clone();//shallow copy
			if(s.key!=null)
				s.key=(KeyFeature)s.key.clone();
			s.spatial=new ArrayList<SpatialFeature>();
			for(SpatialFeature f:spatial)
				s.spatial.add((SpatialFeature)f.clone());
			s.target=new ArrayList<Feature>();
			for(Feature f:target)
				s.target.add((Feature)f.clone());
			
			return s;
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
			
}
	


}
