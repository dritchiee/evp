package pl.enterprise.vpn.client.logic;

import android.content.Context;
import android.content.Intent;
import android.content.RestrictionsManager;
import android.os.Bundle;
import android.util.Base64;

import pl.enterprise.vpn.client.data.Prefs;
import pl.enterprise.vpn.client.data.VpnProfile;
import pl.enterprise.vpn.client.data.VpnProfileDataSource;
import pl.enterprise.vpn.client.data.VpnType;
import pl.enterprise.vpn.client.ui.MainActivity;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

class ManagedConfigurationController implements ManagedConfigurationContract.Controller {

    private final RestrictionsManager restrictionsManager;

    ManagedConfigurationController() {
        restrictionsManager = (RestrictionsManager) StrongSwanApplication.getContext().getSystemService(Context.RESTRICTIONS_SERVICE);
    }

    @Override
    public void onServiceStarted() {
        Bundle bundle = restrictionsManager.getApplicationRestrictions();
        //default value is empty string or null when application is running not in managed work space
        //when any value is set that mean there is managed configuration
        String host = bundle.getString(HOST);
        if (host != null && !host.isEmpty()) {
            //save new configuration because it could change before application started
            saveAndStartNewVpnProfile(bundle);
        }
    }

    @Override
    public void onConfigurationChange() {
        Bundle bundle = restrictionsManager.getApplicationRestrictions();
        saveAndStartNewVpnProfile(bundle);
    }

    private void saveAndStartNewVpnProfile(Bundle bundle) {
        VpnProfile vpnProfile = new VpnProfile();
        vpnProfile.setGateway(bundle.getString(HOST));

        vpnProfile.setVpnType(getVpnTypeFromString(bundle.getString(VPN_TYPE)));

        String userCertData = bundle.getString(USER_CERT_DATA);
        String userCertPass = bundle.getString(USER_CERT_PASS);
        vpnProfile.setUserCertificateAlias(getUserCertAliasFromCertData(userCertData, userCertPass));
        vpnProfile.setUserCertificatePassword(userCertPass);
        vpnProfile.setUserCertificateData(userCertData);

        vpnProfile.setCertificateAlias(storeCaCertificate(bundle.getString(CA_CERT_DATA)));
        vpnProfile.setName(bundle.getString(PROFILE_NAME));
        if (bundle.getBoolean(USE_ADVANCED_SETTINGS)) {
            vpnProfile.setRemoteId(bundle.getString(ADVANCED_SERVER_IDENTITY));
            vpnProfile.setMTU(bundle.getInt(ADVANCED_MTU));
            vpnProfile.setPort(bundle.getInt(ADVANCED_PORT));
            vpnProfile.setNATKeepAlive(bundle.getInt(ADVANCED_NAT_KEEP_ALIVE));
            vpnProfile.setFlags(bundle.getBoolean(ADVANCED_SEND_CERT_REQUEST) ? VpnProfile.FLAGS_SUPPRESS_CERT_REQS : 0);
            vpnProfile.setIncludedSubnets(bundle.getString(ADVANCED_SUBNETS_WHITELIST));
            vpnProfile.setExcludedSubnets(bundle.getString(ADVANCED_SUBNETS_BLACKLIST));

            int splitTunneling = 0;
            splitTunneling |= bundle.getBoolean(ADVANCED_BLOCK_IP_V4) ? VpnProfile.SPLIT_TUNNELING_BLOCK_IPV4 : 0;
            splitTunneling |= bundle.getBoolean(ADVANCED_BLOCK_IP_V6) ? VpnProfile.SPLIT_TUNNELING_BLOCK_IPV6 : 0;
            vpnProfile.setSplitTunneling(splitTunneling == 0 ? null : splitTunneling);

            String appsBehavior = bundle.getString(ADVANCED_APPS_BEHAVIOR);
            if (appsBehavior != null) {
                vpnProfile.setSelectedAppsHandling(Integer.valueOf(appsBehavior));
            }
            vpnProfile.setSelectedApps(bundle.getString(ADVANCED_APPS_LIST));
        }

        Prefs.put(ALLOW_MODIFY_VPN_PROFILE, bundle.getBoolean(ALLOW_MODIFY_VPN_PROFILE));
        Prefs.put(HAS_MANAGED_CONFIG, true);
        Prefs.put(CLOSE_APP_AFTER_CONNECT, bundle.getBoolean(CLOSE_APP_AFTER_CONNECT));
        Prefs.put(ALLOW_DISCONNECT, bundle.getBoolean(ALLOW_DISCONNECT));
        Prefs.put(ALLOW_CLEAR_APP_DATA, bundle.getBoolean(ALLOW_CLEAR_APP_DATA));

        VpnProfileDataSource dataSource = new VpnProfileDataSource(StrongSwanApplication.getContext());
        dataSource.open();
        for (VpnProfile profile : dataSource.getAllVpnProfiles()) {
            dataSource.deleteVpnProfile(profile);
        }
        vpnProfile = dataSource.insertProfile(vpnProfile);
        dataSource.close();


        StrongSwanApplication.getContext().startActivity(new Intent(StrongSwanApplication.getContext(), MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .setAction(MainActivity.START_PROFILE).putExtra(MainActivity.EXTRA_VPN_PROFILE_ID, vpnProfile.getId()));
    }

    private VpnType getVpnTypeFromString(String vpnTypeAsString) {
        switch (vpnTypeAsString) {
            case "0":
                return VpnType.IKEV2_EAP;
            case "1":
                return VpnType.IKEV2_CERT;
            case "2":
                return VpnType.IKEV2_CERT_EAP;
            case "3":
                return VpnType.IKEV2_EAP_TLS;
            case "4":
                return VpnType.IKEV2_BYOD_EAP;
            default:
                return null;
        }
    }

    /**
     * @param certData used to create tmp_file.cer
     * @return certificate alias
     */
    private String storeCaCertificate(String certData) {
        String alias = "";
        try {
            KeyStore store = KeyStore.getInstance("LocalCertificateStore");
            store.load(null, null);
            store.setCertificateEntry(null, extractCertificate(certData));
            alias = store.aliases().nextElement();
            TrustedCertificateManager.getInstance().reset();
        } catch (CertificateException | NoSuchAlgorithmException | IOException | KeyStoreException ignored) {
        }
        return alias;
    }

    private X509Certificate extractCertificate(String certData) throws CertificateException {
        byte[]             bits    = Base64.decode(certData, 0);
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        return (X509Certificate) factory.generateCertificate(new ByteArrayInputStream(bits));
    }

    private String getUserCertAliasFromCertData(String certData, String certPass) {
        String alias = "";
        try {
            KeyStore ks = KeyStore.getInstance("PKCS12");
            ks.load(new ByteArrayInputStream(Base64.decode(certData, 0)), certPass.toCharArray());
            alias = ks.aliases().nextElement();
        } catch (CertificateException | NoSuchAlgorithmException | IOException | KeyStoreException ignored) {
        }
        return alias;
    }
}
