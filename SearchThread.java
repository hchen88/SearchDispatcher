package server.dispatcher;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import entities.Artist;
import entities.Song;
import entities.Track;
import server.DFS.ChordMessageInterface;
import server.DFS.DFS;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class SearchThread implements Runnable {
    String name, option, text;
    String Results;
    Long time;
    int counter;
    int page;
    DFS.FileJson file;
    DFS dfs;


    public SearchThread(int page, DFS.FileJson file, DFS dfs, String option, String text, int counter) {
        this.page = page;
        this.file = file;
        this.dfs = dfs;
        this.option = option;
        this.text = text;
        this.counter = counter;
    }

    public String getResults(){
        return Results;
    }

    public int getCounter(){
        return counter;
    }
    @Override
    public void run() {
        counter++;
        List<Track> tracks = null;
        Long pageGuid = this.file.getPages().get(page).getGuid();
        Gson gson = null;
        Type type = new TypeToken<List<Track>>() {}.getType();
        try {
            ChordMessageInterface succ = dfs.getChord().locateSuccessor(pageGuid);
            System.out.println(Long.toString(succ.getId()) + "/repository/" + Long.toString(pageGuid));
            FileReader reader = new FileReader(Long.toString(succ.getId()) + "/repository/" + Long.toString(pageGuid));
            tracks = gson.fromJson(reader, type);
            reader.close();
        }catch(FileNotFoundException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(tracks.size());
            Song song;
            Artist artist;
            List<Song> songs = new ArrayList<>();

            for(Track t : tracks){
                if(option.equals("Song")){
                    song = t.getSong();
                    if(song.getTitle().contains(text)){
                        songs.add(song);
                        break;
                    }
                }
                else if(option.equals("Artist")){
                    artist = t.getArtist();
                    if(artist.getName().contains(text)){
                        songs.add(t.getSong());
                    }
                }
                else{
                    artist = t.getArtist();
                    if(artist.getTerms().contains(text)){
                        songs.add(t.getSong());
                    }
                }
            }

            this.Results = gson.toJson(songs);
            counter--;
            System.out.println("Thread " + this.page + " finished running! counter at: " + this.counter);
    }
}
