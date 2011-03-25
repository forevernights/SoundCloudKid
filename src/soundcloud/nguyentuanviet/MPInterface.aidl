   
package soundcloud.nguyentuanviet;      
   
      interface MPInterface {
   
              void clearPlaylist();
  
              void addSongPlaylist( in String song );
  
              void playFile( in int position );       
  
              void pause();
 
              void stop();
 
              void skipForward();

              void skipBack();
              
              void test(in String track);
              
              int getCurrentDuration();
              
              int getDuration();
              
      }
