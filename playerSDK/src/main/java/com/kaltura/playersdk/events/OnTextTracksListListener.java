package com.kaltura.playersdk.events;

import java.util.ArrayList;
import java.util.List;

public abstract class OnTextTracksListListener extends Listener{
    @Override
    final protected void setEventType() {
        mEventType = EventType.TEXT_TRACK_LIST_LISTENER_TYPE;
    }

    @Override
    final protected void executeInternalCallback(InputObject inputObject){
        TextTracksListInputObject input = (TextTracksListInputObject) inputObject;
        onTextTracksList(input.languages, input.defaultTrackIndex);
    }

    @Override
    final protected boolean checkValidInputObjectType(InputObject inputObject){
        return inputObject instanceof TextTracksListInputObject;
    }



    abstract public void onTextTracksList( List<LanguageItem> list, int defaultTrackIndex );

    public static class LanguageItem{
        String src;
        String label;
    }

    public static class TextTracksListInputObject extends InputObject{
        public List<LanguageItem> languages;
        public int defaultTrackIndex;

        public void setLanguages(List<String> languagesList){
            languages = new ArrayList<>();
            for (String str : languagesList){
                LanguageItem lngItm = new LanguageItem();
                lngItm.src = str;
                lngItm.label = str;
                languages.add(lngItm);
            }
        }

    }
}
