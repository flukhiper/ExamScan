package th.co.banana.scan.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by MSILeopardPro on 14/2/2560.
 */

public class CustomGallery implements Parcelable {
    public String sdcardPath;
    public boolean isSeleted = false;

    public CustomGallery() {
    }

    public CustomGallery(boolean isSeleted, String sdcardPath) {
        this.isSeleted = isSeleted;
        this.sdcardPath = sdcardPath;
    }

    protected CustomGallery(Parcel in) {
        sdcardPath = in.readString();
        isSeleted = in.readByte() != 0;
    }

    public static final Creator<CustomGallery> CREATOR = new Creator<CustomGallery>() {
        @Override
        public CustomGallery createFromParcel(Parcel in) {
            return new CustomGallery(in);
        }

        @Override
        public CustomGallery[] newArray(int size) {
            return new CustomGallery[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(sdcardPath);
        dest.writeByte((byte) (isSeleted ? 1 : 0));
    }
}
