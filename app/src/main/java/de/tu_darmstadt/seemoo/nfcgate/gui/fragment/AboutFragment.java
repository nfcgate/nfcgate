package de.tu_darmstadt.seemoo.nfcgate.gui.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import de.tu_darmstadt.seemoo.nfcgate.BuildConfig;
import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;
import de.tu_darmstadt.seemoo.nfcgate.R;

public class AboutFragment extends Fragment {
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // custom elements
        Element versionElement = new Element()
                .setIconDrawable(R.drawable.ic_about_black_24dp)
                .setTitle(getString(R.string.about_version, BuildConfig.VERSION_NAME));
        Element licenseElement = new Element()
                .setIconDrawable(R.drawable.ic_copyright_black_24dp)
                .setIntent(new Intent()
                    .setAction(Intent.ACTION_VIEW)
                    .addCategory(Intent.CATEGORY_BROWSABLE)
                    .setData(Uri.parse("https://www.apache.org/licenses/LICENSE-2.0")))
                .setTitle(getString(R.string.about_license));


        // create actual page
        View aboutPage = new AboutPage(getContext())
                .setImage(R.mipmap.ic_launcher)
                .addGitHub("nfcgate/nfcgate")
                .addItem(licenseElement)
                .addItem(versionElement)
                .create();

        // workaround to set HTML description
        TextView description = aboutPage.findViewById(R.id.description);
        description.setText(Html.fromHtml(getString(R.string.about_text)));
        return aboutPage;
    }
}
