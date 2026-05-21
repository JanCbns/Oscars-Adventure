import javax.sound.sampled.*;
import java.io.File;

public class AudioManager {

    private static Clip bgMusic;

    public static void playSound(String path) {
        try {
            AudioInputStream audio = AudioSystem.getAudioInputStream(new File(path));
            Clip clip = AudioSystem.getClip();
            clip.open(audio);
            clip.start();
        } catch (Exception e) {
            System.out.println("Error playing sound: " + path);
        }
    }

    public static void playMusic(String path) {
        try {
            AudioInputStream audio = AudioSystem.getAudioInputStream(new File(path));
            bgMusic = AudioSystem.getClip();
            bgMusic.open(audio);
            bgMusic.loop(Clip.LOOP_CONTINUOUSLY);
            bgMusic.start();
        } catch (Exception e) {
            System.out.println("Error playing music: " + path);
        }
    }

    public static void stopMusic() {
        if (bgMusic != null) {
            bgMusic.stop();
        }
    }
}