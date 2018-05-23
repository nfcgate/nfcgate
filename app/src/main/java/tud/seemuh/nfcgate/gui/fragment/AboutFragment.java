package tud.seemuh.nfcgate.gui.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;
import tud.seemuh.nfcgate.R;

public class AboutFragment extends Fragment implements BaseFragment {
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // custom elements
        Element versionElement = new Element()
                .setIconDrawable(R.drawable.ic_about_black_24dp)
                .setTitle("Version 2.0");
        Element licenseElement = new Element()
                .setIconDrawable(R.drawable.ic_copyright_black_24dp)
                .setIntent(new Intent()
                    .setAction(Intent.ACTION_VIEW)
                    .addCategory(Intent.CATEGORY_BROWSABLE)
                    .setData(Uri.parse("https://www.apache.org/licenses/LICENSE-2.0")))
                .setTitle("Apache License v2.0");


        // create actual page
        View aboutPage = new AboutPage(getContext())
                .setImage(R.drawable.ic_launcher)
                .addGitHub("nfcgate/nfcgate")
                .addItem(licenseElement)
                .addItem(versionElement)
                .create();

        // workaround to set HTML description
        TextView description = aboutPage.findViewById(R.id.description);
        description.setText(Html.fromHtml(getString(R.string.about)));
        return aboutPage;
    }

    @Override
    public String getTagName() {
        return "about";
    }
}
