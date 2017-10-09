package org.strongswan.android.logic;


public interface ManagedConfigurationContract {
    interface Controller {
        String ALLOW_MODIFY_VPN_PROFILE   = "allow_modify_vpn_profile";
        String ALLOW_DISCONNECT           = "allow_disconnect";
        String ALLOW_CLEAR_APP_DATA       = "allow_clear_app_data";
        String HOST                       = "host";
        String VPN_TYPE                   = "vpn_type";
        String USER_CERT_DATA             = "user_cert_data";
        String USER_CERT_PASS             = "user_cert_pass";
        String USER_CERT_ALIAS            = "user_cert_alias";
        String CA_CERT_DATA               = "ca_cert_data";
        String PROFILE_NAME               = "profile_name";
        String USE_ADVANCED_SETTINGS      = "use_advanced_settings";
        String ADVANCED_SERVER_IDENTITY   = "advanced_server_identity";
        String ADVANCED_MTU               = "advanced_mtu";
        String ADVANCED_PORT              = "advanced_port";
        String ADVANCED_NAT_KEEP_ALIVE    = "advanced_nat_keep_alive";
        String ADVANCED_SEND_CERT_REQUEST = "advanced_send_certificate_request";
        String ADVANCED_SUBNETS_WHITELIST = "advanced_subnets_whitelist";
        String ADVANCED_SUBNETS_BLACKLIST = "advanced_subnets_blacklist";
        String ADVANCED_BLOCK_IP_V4       = "advanced_block_ipv4";
        String ADVANCED_BLOCK_IP_V6       = "advanced_block_ipv6";
        String ADVANCED_APPS_BEHAVIOR     = "advanced_applications_behavior";
        String ADVANCED_APPS_LIST         = "advanced_applications_list";

        String HAS_MANAGED_CONFIG      = "has_managed_config";
        String CLOSE_APP_AFTER_CONNECT = "close_app_after_vpn_connect";


        void onServiceStarted();

        void onConfigurationChange();
    }
}
