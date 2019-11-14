package server.dispatcher;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import entities.*;
import server.DFS.ChordMessageInterface;
import server.DFS.DFS;
import server.DFS.DFS.FileJson;
import server.DFS.DFS.FilesJson;
import server.DFS.DFS.PagesJson;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SearchDispatcher
{
    private Gson gson;
    private DFS dfs;

    public SearchDispatcher()
    {
        gson = new GsonBuilder().setPrettyPrinting().create();
    }
    
    public SearchDispatcher(DFS dfs)
    {
        gson = new GsonBuilder().setPrettyPrinting().create();
        this.dfs = dfs;
    }
    /**
     * this method searches for a specific song/artist
     * @param text - the string of text.
     * @param option - Artist/Song type to search for as a string
     * @throws Exception 
     */
    public String search(String text, String option) throws Exception {
        List<Track> tracks = null;
        FileJson fileToSerach = null;
        ArrayList<SearchThread> searchThreads = new ArrayList<>();
        String songs = "";

        int j = 0;
        Type type = new TypeToken<List<Track>>() {
        }.getType();
        try {
            FilesJson files = dfs.readMetaData();
            PagesJson dir = null;
            for (FileJson file : files.getFile()) {
                if (file.getName().equals("music.json")) {
                    fileToSerach = file;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println("Before Threads created");
        for (int i = 0; i < fileToSerach.getPages().size(); i++) {
            SearchThread t = new SearchThread(i, fileToSerach, dfs, option, text, j);
            Thread object = new Thread(t);
            searchThreads.add(t);
            object.start();
        }

        while (j != 0) {

        }

        for (int i = 0; i < searchThreads.size(); i++) {
            songs += searchThreads.get(i).getResults();
        }
        System.out.println(songs);
        return gson.toJson(songs);
    }


    public String loadRecommended(String username) throws Exception
    {
        List<Profile> profiles = null;
        Type type = new TypeToken<List<Profile>>(){}.getType();
        try {
        	FilesJson files = dfs.readMetaData();
        	PagesJson dir = null;
        	for(FileJson file : files.getFile())
        	{
        		if(file.getName().equals("profiles.json"))
        			dir = file.getPages().get(0);
        	}
        	ChordMessageInterface succ = dfs.getChord().locateSuccessor(dir.getGuid());
            FileReader reader = new FileReader(Long.toString(succ.getId()) +"/repository/"+ Long.toString(dir.getGuid()));
            profiles = gson.fromJson(reader, type);
            reader.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        if (profiles == null) {
            profiles = new ArrayList<>();
        }

        Recommended recommended = null;
        for(Profile p : profiles){
            if(p.getUsername().equals(username)){
                recommended = p.getRecommended();
            }
        }

        List<Track> tracks = null;
        type = new TypeToken<List<Track>>(){}.getType();
        try {
        	FilesJson files = dfs.readMetaData();
        	PagesJson dir = null;
        	for(FileJson file : files.getFile())
        	{
        		if(file.getName().equals("music.json"))
        			dir = file.getPages().get(0); //only seraches the first page
        	}
        	ChordMessageInterface succ = dfs.getChord().locateSuccessor(dir.getGuid());
            FileReader reader = new FileReader(Long.toString(succ.getId()) +"/repository/"+ Long.toString(dir.getGuid()));
            tracks = gson.fromJson(reader, type);
            reader.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        Song song;
        List<Song> songs = new ArrayList<>();
        for(Track t : tracks){
            double tempo = t.getSong().getTempo();
            double loudness = t.getSong().getLoudness();
            if(tempo >= 0.97 * recommended.getTempo() && tempo <= 1.03 * recommended.getTempo()){
                if(loudness <= 0.97 * recommended.getLoudness() && loudness >= 1.03 * recommended.getLoudness()){
                    songs.add(t.getSong());
                }
            }
        }

        return gson.toJson(songs);
    }
}
