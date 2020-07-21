package org.odk.collect.android.application.initialization;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.gms.maps.GoogleMap;
import com.mapbox.mapboxsdk.maps.Style;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static org.odk.collect.android.application.initialization.migration.SharedPreferenceUtils.assertPrefs;
import static org.odk.collect.android.application.initialization.migration.SharedPreferenceUtils.assertPrefsEmpty;
import static org.odk.collect.android.application.initialization.migration.SharedPreferenceUtils.initPrefs;

@RunWith(RobolectricTestRunner.class)
public class CollectPreferenceMigratorTest {

    private SharedPreferences generalPrefs;
    private SharedPreferences adminPrefs;
    private SharedPreferences metaPrefs;

    @Before
    public void setUp() throws Exception {
        generalPrefs = getApplicationContext().getSharedPreferences("generalTest", Context.MODE_PRIVATE);
        adminPrefs = getApplicationContext().getSharedPreferences("adminTest", Context.MODE_PRIVATE);
        metaPrefs = getApplicationContext().getSharedPreferences("metaTest", Context.MODE_PRIVATE);
    }

    @Test
    public void shouldMigrateGoogleMapSettings() {
        initPrefs(generalPrefs, "map_sdk_behavior", "google_maps", "map_basemap_behavior", "streets");
        runMigrations();
        assertPrefs(generalPrefs, "basemap_source", "google", "google_map_style", String.valueOf(GoogleMap.MAP_TYPE_NORMAL));

        initPrefs(generalPrefs, "map_sdk_behavior", "google_maps", "map_basemap_behavior", "satellite");
        runMigrations();
        assertPrefs(generalPrefs, "basemap_source", "google", "google_map_style", String.valueOf(GoogleMap.MAP_TYPE_SATELLITE));

        initPrefs(generalPrefs, "map_sdk_behavior", "google_maps", "map_basemap_behavior", "terrain\u200e");
        runMigrations();
        assertPrefs(generalPrefs, "basemap_source", "google", "google_map_style", String.valueOf(GoogleMap.MAP_TYPE_TERRAIN));

        initPrefs(generalPrefs, "map_sdk_behavior", "google_maps", "map_basemap_behavior", "hybrid");
        runMigrations();
        assertPrefs(generalPrefs, "basemap_source", "google", "google_map_style", String.valueOf(GoogleMap.MAP_TYPE_HYBRID));
    }

    @Test
    public void shouldMigrateMapboxMapSettings() {
        initPrefs(generalPrefs, "map_sdk_behavior", "mapbox_maps", "map_basemap_behavior", "mapbox_streets");
        runMigrations();
        assertPrefs(generalPrefs, "basemap_source", "mapbox", "mapbox_map_style", Style.MAPBOX_STREETS);

        initPrefs(generalPrefs, "map_sdk_behavior", "mapbox_maps", "map_basemap_behavior", "mapbox_light");
        runMigrations();
        assertPrefs(generalPrefs, "basemap_source", "mapbox", "mapbox_map_style", Style.LIGHT);

        initPrefs(generalPrefs, "map_sdk_behavior", "mapbox_maps", "map_basemap_behavior", "mapbox_dark");
        runMigrations();
        assertPrefs(generalPrefs, "basemap_source", "mapbox", "mapbox_map_style", Style.DARK);

        initPrefs(generalPrefs, "map_sdk_behavior", "mapbox_maps", "map_basemap_behavior", "mapbox_satellite");
        runMigrations();
        assertPrefs(generalPrefs, "basemap_source", "mapbox", "mapbox_map_style", Style.SATELLITE);

        initPrefs(generalPrefs, "map_sdk_behavior", "mapbox_maps", "map_basemap_behavior", "mapbox_satellite_streets");
        runMigrations();
        assertPrefs(generalPrefs, "basemap_source", "mapbox", "mapbox_map_style", Style.SATELLITE_STREETS);

        initPrefs(generalPrefs, "map_sdk_behavior", "mapbox_maps", "map_basemap_behavior", "mapbox_outdoors");
        runMigrations();
        assertPrefs(generalPrefs, "basemap_source", "mapbox", "mapbox_map_style", Style.OUTDOORS);
    }

    @Test
    public void shouldMigrateOsmMapSettings() {
        initPrefs(generalPrefs, "map_sdk_behavior", "osmdroid", "map_basemap_behavior", "openmap_streets");
        new CollectPreferenceMigrator(generalPrefs, adminPrefs, metaPrefs).migrate();
        assertPrefs(generalPrefs, "basemap_source", "osm");

        initPrefs(generalPrefs, "map_sdk_behavior", "osmdroid", "map_basemap_behavior", "openmap_usgs_topo");
        runMigrations();
        assertPrefs(generalPrefs, "basemap_source", "usgs", "usgs_map_style", "topographic");

        initPrefs(generalPrefs, "map_sdk_behavior", "osmdroid", "map_basemap_behavior", "openmap_usgs_sat");
        runMigrations();
        assertPrefs(generalPrefs, "basemap_source", "usgs", "usgs_map_style", "hybrid");

        initPrefs(generalPrefs, "map_sdk_behavior", "osmdroid", "map_basemap_behavior", "openmap_usgs_img");
        runMigrations();
        assertPrefs(generalPrefs, "basemap_source", "usgs", "usgs_map_style", "satellite");

        initPrefs(generalPrefs, "map_sdk_behavior", "osmdroid", "map_basemap_behavior", "openmap_stamen_terrain");
        runMigrations();
        assertPrefs(generalPrefs, "basemap_source", "stamen");

        initPrefs(generalPrefs, "map_sdk_behavior", "osmdroid", "map_basemap_behavior", "openmap_cartodb_positron");
        runMigrations();
        assertPrefs(generalPrefs, "basemap_source", "carto", "carto_map_style", "positron");

        initPrefs(generalPrefs, "map_sdk_behavior", "osmdroid", "map_basemap_behavior", "openmap_cartodb_darkmatter");
        runMigrations();
        assertPrefs(generalPrefs, "basemap_source", "carto", "carto_map_style", "dark_matter");
    }

    @Test
    public void shouldMigrateAdminSettings() {
        initPrefs(adminPrefs, "unrelated", "value");
        runMigrations();
        assertPrefs(adminPrefs, "unrelated", "value");

        initPrefs(adminPrefs, "show_map_sdk", true);
        runMigrations();
        assertPrefs(adminPrefs, "show_map_sdk", true);

        initPrefs(adminPrefs, "show_map_sdk", false);
        runMigrations();
        assertPrefs(adminPrefs, "maps", false);

        initPrefs(adminPrefs, "show_map_basemap", true);
        runMigrations();
        assertPrefs(adminPrefs, "show_map_basemap", true);

        initPrefs(adminPrefs, "show_map_basemap", false);
        runMigrations();
        assertPrefs(adminPrefs, "maps", false);
    }

    @Test
    public void migratesMetaKeysToMetaPrefs() {
        initPrefs(generalPrefs,
                "firstRun", true,
                "lastVersion", 1L,
                "scoped_storage_used", true,
                "metadata_migrated", true,
                "mapbox_initialized", true
        );

        runMigrations();

        assertPrefsEmpty(generalPrefs);
        assertPrefs(metaPrefs,
                "scoped_storage_used", true,
                "mapbox_initialized", true
        );
    }

    @Test
    public void migratesServerType() {
        initPrefs(generalPrefs, "protocol", "other_protocol");
        runMigrations();
        assertPrefs(generalPrefs, "protocol", "odk_default");
    }

    @Test
    public void migratesAutosendSettings() {
        initPrefs(generalPrefs,
                "autosend_wifi", false,
                "autosend_network", false
        );
        runMigrations();
        assertPrefs(generalPrefs,
                "autosend", "off"
        );

        initPrefs(generalPrefs,
                "autosend_wifi", true,
                "autosend_network", false
        );
        runMigrations();
        assertPrefs(generalPrefs,
                "autosend", "wifi_only"
        );

        initPrefs(generalPrefs,
                "autosend_wifi", false,
                "autosend_network", true
        );
        runMigrations();
        assertPrefs(generalPrefs,
                "autosend", "cellular_only"
        );

        initPrefs(generalPrefs,
                "autosend_wifi", true,
                "autosend_network", true
        );
        runMigrations();
        assertPrefs(generalPrefs,
                "autosend", "wifi_and_cellular"
        );
    }

    private void runMigrations() {
        new CollectPreferenceMigrator(generalPrefs, adminPrefs, metaPrefs).migrate();
    }
}