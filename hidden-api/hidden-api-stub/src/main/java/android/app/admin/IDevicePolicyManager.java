package android.app.admin;
import android.accounts.Account;
import android.app.IApplicationThread;
import android.app.IServiceConnection;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.IPackageDataObserver;
import android.content.pm.ParceledListSlice;
import android.content.pm.StringParceledListSlice;
import android.graphics.Bitmap;
import android.net.ProxyInfo;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.os.RemoteCallback;
import android.os.UserHandle;
import android.telephony.data.ApnSetting;

import java.util.List;

public interface IDevicePolicyManager {

    void setPasswordQuality(ComponentName who, int quality, boolean parent);
    int getPasswordQuality(ComponentName who, int userHandle, boolean parent);

    void setPasswordMinimumLength(ComponentName who, int length, boolean parent);
    int getPasswordMinimumLength(ComponentName who, int userHandle, boolean parent);

    void setPasswordMinimumUpperCase(ComponentName who, int length, boolean parent);
    int getPasswordMinimumUpperCase(ComponentName who, int userHandle, boolean parent);

    void setPasswordMinimumLowerCase(ComponentName who, int length, boolean parent);
    int getPasswordMinimumLowerCase(ComponentName who, int userHandle, boolean parent);

    void setPasswordMinimumLetters(ComponentName who, int length, boolean parent);
    int getPasswordMinimumLetters(ComponentName who, int userHandle, boolean parent);

    void setPasswordMinimumNumeric(ComponentName who, int length, boolean parent);
    int getPasswordMinimumNumeric(ComponentName who, int userHandle, boolean parent);

    void setPasswordMinimumSymbols(ComponentName who, int length, boolean parent);
    int getPasswordMinimumSymbols(ComponentName who, int userHandle, boolean parent);

    void setPasswordMinimumNonLetter(ComponentName who, int length, boolean parent);
    int getPasswordMinimumNonLetter(ComponentName who, int userHandle, boolean parent);

//    PasswordMetrics getPasswordMinimumMetrics(int userHandle, boolean deviceWideOnly);

    void setPasswordHistoryLength(ComponentName who, int length, boolean parent);
    int getPasswordHistoryLength(ComponentName who, int userHandle, boolean parent);

    void setPasswordExpirationTimeout(ComponentName who, long expiration, boolean parent);
    long getPasswordExpirationTimeout(ComponentName who, int userHandle, boolean parent);

    long getPasswordExpiration(ComponentName who, int userHandle, boolean parent);

    boolean isActivePasswordSufficient(int userHandle, boolean parent);
    boolean isActivePasswordSufficientForDeviceRequirement();
    boolean isPasswordSufficientAfterProfileUnification(int userHandle, int profileUser);
    int getPasswordComplexity(boolean parent);
    void setRequiredPasswordComplexity(int passwordComplexity, boolean parent);
    int getRequiredPasswordComplexity(boolean parent);
    int getAggregatedPasswordComplexityForUser(int userId, boolean deviceWideOnly);
    boolean isUsingUnifiedPassword(ComponentName admin);
    int getCurrentFailedPasswordAttempts(int userHandle, boolean parent);
    int getProfileWithMinimumFailedPasswordsForWipe(int userHandle, boolean parent);

    void setMaximumFailedPasswordsForWipe(ComponentName admin, int num, boolean parent);
    int getMaximumFailedPasswordsForWipe(ComponentName admin, int userHandle, boolean parent);

    boolean resetPassword(String password, int flags);

    void setMaximumTimeToLock(ComponentName who, long timeMs, boolean parent);
    long getMaximumTimeToLock(ComponentName who, int userHandle, boolean parent);

    void setRequiredStrongAuthTimeout(ComponentName who, long timeMs, boolean parent);
    long getRequiredStrongAuthTimeout(ComponentName who, int userId, boolean parent);

    void lockNow(int flags, boolean parent);

    void wipeDataWithReason(int flags, String wipeReasonForUser, boolean parent);

    void setFactoryResetProtectionPolicy(ComponentName who, FactoryResetProtectionPolicy policy);
    FactoryResetProtectionPolicy getFactoryResetProtectionPolicy(ComponentName who);
    boolean isFactoryResetProtectionPolicySupported();

//    void sendLostModeLocationUpdate(AndroidFuture<boolean> future);

    ComponentName setGlobalProxy(ComponentName admin, String proxySpec, String exclusionList);
    ComponentName getGlobalProxyAdmin(int userHandle);
    void setRecommendedGlobalProxy(ComponentName admin, ProxyInfo proxyInfo);

    int setStorageEncryption(ComponentName who, boolean encrypt);
    boolean getStorageEncryption(ComponentName who, int userHandle);
    int getStorageEncryptionStatus(String callerPackage, int userHandle);

    boolean requestBugreport(ComponentName who);

    void setCameraDisabled(ComponentName who, boolean disabled, boolean parent);
    boolean getCameraDisabled(ComponentName who, int userHandle, boolean parent);

    void setScreenCaptureDisabled(ComponentName who, boolean disabled, boolean parent);
    boolean getScreenCaptureDisabled(ComponentName who, int userHandle, boolean parent);

    void setNearbyNotificationStreamingPolicy(int policy);
    int getNearbyNotificationStreamingPolicy(int userId);

    void setNearbyAppStreamingPolicy(int policy);
    int getNearbyAppStreamingPolicy(int userId);

    void setKeyguardDisabledFeatures(ComponentName who, int which, boolean parent);
    int getKeyguardDisabledFeatures(ComponentName who, int userHandle, boolean parent);

    void setActiveAdmin(ComponentName policyReceiver, boolean refreshing, int userHandle);
    boolean isAdminActive(ComponentName policyReceiver, int userHandle);
    List<ComponentName> getActiveAdmins(int userHandle);
    
    boolean packageHasActiveAdmins(String packageName, int userHandle);
    void getRemoveWarning(ComponentName policyReceiver, RemoteCallback result, int userHandle);
    void removeActiveAdmin(ComponentName policyReceiver, int userHandle);
    void forceRemoveActiveAdmin(ComponentName policyReceiver, int userHandle);
    boolean hasGrantedPolicy(ComponentName policyReceiver, int usesPolicy, int userHandle);

//    void reportPasswordChanged(PasswordMetrics metrics, int userId);
    
    void reportFailedPasswordAttempt(int userHandle);
    void reportSuccessfulPasswordAttempt(int userHandle);
    void reportFailedBiometricAttempt(int userHandle);
    void reportSuccessfulBiometricAttempt(int userHandle);
    void reportKeyguardDismissed(int userHandle);
    void reportKeyguardSecured(int userHandle);

    boolean setDeviceOwner(ComponentName who, String ownerName, int userId, boolean setProfileOwnerOnCurrentUserIfNecessary);
    ComponentName getDeviceOwnerComponent(boolean callingUserOnly);
    boolean hasDeviceOwner();
    String getDeviceOwnerName();
    void clearDeviceOwner(String packageName);
    int getDeviceOwnerUserId();

    boolean setProfileOwner(ComponentName who, String ownerName, int userHandle);
    ComponentName getProfileOwnerAsUser(int userHandle);
    ComponentName getProfileOwnerOrDeviceOwnerSupervisionComponent(UserHandle userHandle);
    boolean isSupervisionComponent(ComponentName who);
    String getProfileOwnerName(int userHandle);
    void setProfileEnabled(ComponentName who);
    void setProfileName(ComponentName who, String profileName);
    void clearProfileOwner(ComponentName who);
    boolean hasUserSetupCompleted();
    boolean isOrganizationOwnedDeviceWithManagedProfile();

    boolean checkDeviceIdentifierAccess(String packageName, int pid, int uid);

    void setDeviceOwnerLockScreenInfo(ComponentName who, CharSequence deviceOwnerInfo);
    CharSequence getDeviceOwnerLockScreenInfo();

    String[] setPackagesSuspended(ComponentName admin, String callerPackage, String[] packageNames, boolean suspended);
    boolean isPackageSuspended(ComponentName admin, String callerPackage, String packageName);
    List<String> listPolicyExemptApps();

    boolean installCaCert(ComponentName admin, String callerPackage, byte[] certBuffer);
    void uninstallCaCerts(ComponentName admin, String callerPackage, String[] aliases);
    void enforceCanManageCaCerts(ComponentName admin, String callerPackage);
    boolean approveCaCert(String alias, int userHandle, boolean approval);
    boolean isCaCertApproved(String alias, int userHandle);

    boolean installKeyPair(ComponentName who, String callerPackage, byte[] privKeyBuffer,
                           byte[] certBuffer, byte[] certChainBuffer, String alias, boolean requestAccess,
                           boolean isUserSelectable);
    boolean removeKeyPair(ComponentName who, String callerPackage, String alias);
    boolean hasKeyPair(String callerPackage, String alias);
//    boolean generateKeyPair(ComponentName who, String callerPackage, String algorithm,
//                            ParcelableKeyGenParameterSpec keySpec,
//                            int idAttestationFlags, KeymasterCertificateChaattestationChain);
    boolean setKeyPairCertificate(ComponentName who, String callerPackage, String alias,
                                  byte[] certBuffer, byte[] certChainBuffer, boolean isUserSelectable);
    void choosePrivateKeyAlias(int uid, Uri uri, String alias, IBinder aliasCallback);

    void setDelegatedScopes(ComponentName who, String delegatePackage, List<String> scopes);
    List<String> getDelegatedScopes(ComponentName who, String delegatePackage);
    List<String> getDelegatePackages(ComponentName who, String scope);

    void setCertInstallerPackage(ComponentName who, String installerPackage);
    String getCertInstallerPackage(ComponentName who);

    boolean setAlwaysOnVpnPackage(ComponentName who, String vpnPackage, boolean lockdown, List<String> lockdownAllowlist);
    String getAlwaysOnVpnPackage(ComponentName who);
    String getAlwaysOnVpnPackageForUser(int userHandle);
    boolean isAlwaysOnVpnLockdownEnabled(ComponentName who);
    boolean isAlwaysOnVpnLockdownEnabledForUser(int userHandle);
    List<String> getAlwaysOnVpnLockdownAllowlist(ComponentName who);

    void addPersistentPreferredActivity(ComponentName admin, IntentFilter filter, ComponentName activity);
    void clearPackagePersistentPreferredActivities(ComponentName admin, String packageName);

    void setDefaultSmsApplication(ComponentName admin, String packageName, boolean parent);

    void setApplicationRestrictions(ComponentName who, String callerPackage, String packageName, Bundle settings);
    Bundle getApplicationRestrictions(ComponentName who, String callerPackage, String packageName);
    boolean setApplicationRestrictionsManagingPackage(ComponentName admin, String packageName);
    String getApplicationRestrictionsManagingPackage(ComponentName admin);
    boolean isCallerApplicationRestrictionsManagingPackage(String callerPackage);

    void setRestrictionsProvider(ComponentName who, ComponentName provider);
    ComponentName getRestrictionsProvider(int userHandle);

    void setUserRestriction(ComponentName who, String key, boolean enable, boolean parent);
    Bundle getUserRestrictions(ComponentName who, boolean parent);
    void addCrossProfileIntentFilter(ComponentName admin, IntentFilter filter, int flags);
    void clearCrossProfileIntentFilters(ComponentName admin);

    boolean setPermittedAccessibilityServices(ComponentName admin,List<String> packageList);
    List<String> getPermittedAccessibilityServices(ComponentName admin);
    List<String> getPermittedAccessibilityServicesForUser(int userId);
    boolean isAccessibilityServicePermittedByAdmin(ComponentName admin, String packageName, int userId);

    boolean setPermittedInputMethods(ComponentName admin,List<String> packageList, boolean parent);
    List<String> getPermittedInputMethods(ComponentName admin, boolean parent);
    List<String> getPermittedInputMethodsAsUser(int userId);
    boolean isInputMethodPermittedByAdmin(ComponentName admin, String packageName, int userId, boolean parent);

    boolean setPermittedCrossProfileNotificationListeners(ComponentName admin, List<String> packageList);
    List<String> getPermittedCrossProfileNotificationListeners(ComponentName admin);
    boolean isNotificationListenerServicePermitted(String packageName, int userId);

    Intent createAdminSupportIntent(String restriction);
    Bundle getEnforcingAdminAndUserDetails(int userId,String restriction);
    boolean setApplicationHidden(ComponentName admin, String callerPackage, String packageName, boolean hidden, boolean parent);
    boolean isApplicationHidden(ComponentName admin, String callerPackage, String packageName, boolean parent);

    UserHandle createAndManageUser(ComponentName who, String name, ComponentName profileOwner, PersistableBundle adminExtras, int flags);
    boolean removeUser(ComponentName who, UserHandle userHandle);
    boolean switchUser(ComponentName who, UserHandle userHandle);
    int startUserInBackground(ComponentName who, UserHandle userHandle);
    int stopUser(ComponentName who, UserHandle userHandle);
    int logoutUser(ComponentName who);
    int logoutUserInternal(); // AIDL doesn't allow overloading name (logoutUser())
    int getLogoutUserId();
    List<UserHandle> getSecondaryUsers(ComponentName who);
    void acknowledgeNewUserDisclaimer(int userId);
    boolean isNewUserDisclaimerAcknowledged(int userId);

    void enableSystemApp(ComponentName admin, String callerPackage, String packageName);
    int enableSystemAppWithIntent(ComponentName admin, String callerPackage, Intent intent);
    boolean installExistingPackage(ComponentName admin, String callerPackage, String packageName);

    void setAccountManagementDisabled(ComponentName who, String accountType, boolean disabled, boolean parent);
    String[] getAccountTypesWithManagementDisabled();
    String[] getAccountTypesWithManagementDisabledAsUser(int userId, boolean parent);

    void setSecondaryLockscreenEnabled(ComponentName who, boolean enabled);
    boolean isSecondaryLockscreenEnabled(UserHandle userHandle);

    void setPreferentialNetworkServiceConfigs(
            List<PreferentialNetworkServiceConfig> preferentialNetworkServiceConfigs);
    List<PreferentialNetworkServiceConfig> getPreferentialNetworkServiceConfigs();

    void setLockTaskPackages(ComponentName who, String[] packages);
    String[] getLockTaskPackages(ComponentName who);
    boolean isLockTaskPermitted(String pkg);

    void setLockTaskFeatures(ComponentName who, int flags);
    int getLockTaskFeatures(ComponentName who);

    void setGlobalSetting(ComponentName who, String setting, String value);
    void setSystemSetting(ComponentName who, String setting, String value);
    void setSecureSetting(ComponentName who, String setting, String value);

    void setConfiguredNetworksLockdownState(ComponentName who, boolean lockdown);
    boolean hasLockdownAdminConfiguredNetworks(ComponentName who);

    void setLocationEnabled(ComponentName who, boolean locationEnabled);

    boolean setTime(ComponentName who, long millis);
    boolean setTimeZone(ComponentName who, String timeZone);

    void setMasterVolumeMuted(ComponentName admin, boolean on);
    boolean isMasterVolumeMuted(ComponentName admin);

    void notifyLockTaskModeChanged(boolean isEnabled, String pkg, int userId);

    void setUninstallBlocked(ComponentName admin, String callerPackage, String packageName, boolean uninstallBlocked);
    boolean isUninstallBlocked(ComponentName admin, String packageName);

    void setCrossProfileCallerIdDisabled(ComponentName who, boolean disabled);
    boolean getCrossProfileCallerIdDisabled(ComponentName who);
    boolean getCrossProfileCallerIdDisabledForUser(int userId);
    void setCrossProfileContactsSearchDisabled(ComponentName who, boolean disabled);
    boolean getCrossProfileContactsSearchDisabled(ComponentName who);
    boolean getCrossProfileContactsSearchDisabledForUser(int userId);
    void startManagedQuickContact(String lookupKey, long contactId, boolean isContactIdIgnored, long directoryId, Intent originalIntent);

    void setBluetoothContactSharingDisabled(ComponentName who, boolean disabled);
    boolean getBluetoothContactSharingDisabled(ComponentName who);
    boolean getBluetoothContactSharingDisabledForUser(int userId);

    void setTrustAgentConfiguration(ComponentName admin, ComponentName agent,
                                    PersistableBundle args, boolean parent);
    List<PersistableBundle> getTrustAgentConfiguration(ComponentName admin,
                                                       ComponentName agent, int userId, boolean parent);

    boolean addCrossProfileWidgetProvider(ComponentName admin, String packageName);
    boolean removeCrossProfileWidgetProvider(ComponentName admin, String packageName);
    List<String> getCrossProfileWidgetProviders(ComponentName admin);

    void setAutoTimeRequired(ComponentName who, boolean required);
    boolean getAutoTimeRequired();

    void setAutoTimeEnabled(ComponentName who, boolean enabled);
    boolean getAutoTimeEnabled(ComponentName who);

    void setAutoTimeZoneEnabled(ComponentName who, boolean enabled);
    boolean getAutoTimeZoneEnabled(ComponentName who);

    void setForceEphemeralUsers(ComponentName who, boolean forceEpehemeralUsers);
    boolean getForceEphemeralUsers(ComponentName who);

    boolean isRemovingAdmin(ComponentName adminReceiver, int userHandle);

    void setUserIcon(ComponentName admin, Bitmap icon);

    void setSystemUpdatePolicy(ComponentName who, SystemUpdatePolicy policy);
    SystemUpdatePolicy getSystemUpdatePolicy();
    void clearSystemUpdatePolicyFreezePeriodRecord();

    boolean setKeyguardDisabled(ComponentName admin, boolean disabled);
    boolean setStatusBarDisabled(ComponentName who, boolean disabled);
    boolean getDoNotAskCredentialsOnBoot();

    void notifyPendingSystemUpdate(SystemUpdateInfo info);
    SystemUpdateInfo getPendingSystemUpdate(ComponentName admin);

    void setPermissionPolicy(ComponentName admin, String callerPackage, int policy);
    int  getPermissionPolicy(ComponentName admin);
    void setPermissionGrantState(ComponentName admin, String callerPackage, String packageName,
                                 String permission, int grantState, RemoteCallback resultReceiver);
    int getPermissionGrantState(ComponentName admin, String callerPackage, String packageName, String permission);
    boolean isProvisioningAllowed(String action, String packageName);
    int checkProvisioningPrecondition(String action, String packageName);
    void setKeepUninstalledPackages(ComponentName admin, String callerPackage, List<String> packageList);
    List<String> getKeepUninstalledPackages(ComponentName admin, String callerPackage);
    boolean isManagedProfile(ComponentName admin);
    String getWifiMacAddress(ComponentName admin);
    void reboot(ComponentName admin);

    void setShortSupportMessage(ComponentName admin, CharSequence message);
    CharSequence getShortSupportMessage(ComponentName admin);
    void setLongSupportMessage(ComponentName admin, CharSequence message);
    CharSequence getLongSupportMessage(ComponentName admin);

    CharSequence getShortSupportMessageForUser(ComponentName admin, int userHandle);
    CharSequence getLongSupportMessageForUser(ComponentName admin, int userHandle);

    void setOrganizationColor(ComponentName admin, int color);
    void setOrganizationColorForUser(int color, int userId);
    void clearOrganizationIdForUser(int userHandle);
    int getOrganizationColor(ComponentName admin);
    int getOrganizationColorForUser(int userHandle);

    void setOrganizationName(ComponentName admin, CharSequence title);
    CharSequence getOrganizationName(ComponentName admin);
    CharSequence getDeviceOwnerOrganizationName();
    CharSequence getOrganizationNameForUser(int userHandle);

    int getUserProvisioningState();
    void setUserProvisioningState(int state, int userHandle);

    void setAffiliationIds(ComponentName admin, List<String> ids);
    List<String> getAffiliationIds(ComponentName admin);
    boolean isCallingUserAffiliated();
    boolean isAffiliatedUser(int userId);

    void setSecurityLoggingEnabled(ComponentName admin, String packageName, boolean enabled);
    boolean isSecurityLoggingEnabled(ComponentName admin, String packageName);
    ParceledListSlice retrieveSecurityLogs(ComponentName admin, String packageName);
    ParceledListSlice retrievePreRebootSecurityLogs(ComponentName admin, String packageName);
    long forceNetworkLogs();
    long forceSecurityLogs();

    boolean isUninstallInQueue(String packageName);
    void uninstallPackageWithActiveAdmins(String packageName);

    boolean isDeviceProvisioned();
    boolean isDeviceProvisioningConfigApplied();
    void setDeviceProvisioningConfigApplied();

    void forceUpdateUserSetupComplete(int userId);

    void setBackupServiceEnabled(ComponentName admin, boolean enabled);
    boolean isBackupServiceEnabled(ComponentName admin);

    void setNetworkLoggingEnabled(ComponentName admin, String packageName, boolean enabled);
    boolean isNetworkLoggingEnabled(ComponentName admin, String packageName);
    List<NetworkEvent> retrieveNetworkLogs(ComponentName admin, String packageName, long batchToken);

    boolean bindDeviceAdminServiceAsUser(ComponentName admin,
                                         IApplicationThread caller, IBinder token, Intent service,
                                         IServiceConnection connection, int flags, int targetUserId);
    List<UserHandle> getBindDeviceAdminTargetUsers(ComponentName admin);
    boolean isEphemeralUser(ComponentName admin);

    long getLastSecurityLogRetrievalTime();
    long getLastBugReportRequestTime();
    long getLastNetworkLogRetrievalTime();

    boolean setResetPasswordToken(ComponentName admin, byte[] token);
    boolean clearResetPasswordToken(ComponentName admin);
    boolean isResetPasswordTokenActive(ComponentName admin);
    boolean resetPasswordWithToken(ComponentName admin, String password, byte[] token, int flags);

    boolean isCurrentInputMethodSetByOwner();
    StringParceledListSlice getOwnerInstalledCaCerts(UserHandle user);

    void clearApplicationUserData(ComponentName admin, String packageName, IPackageDataObserver callback);

    void setLogoutEnabled(ComponentName admin, boolean enabled);
    boolean isLogoutEnabled();

    List<String> getDisallowedSystemApps(ComponentName admin, int userId, String provisioningAction);

    void transferOwnership(ComponentName admin, ComponentName target, PersistableBundle bundle);
    PersistableBundle getTransferOwnershipBundle();

    void setStartUserSessionMessage(ComponentName admin, CharSequence startUserSessionMessage);
    void setEndUserSessionMessage(ComponentName admin, CharSequence endUserSessionMessage);
    CharSequence getStartUserSessionMessage(ComponentName admin);
    CharSequence getEndUserSessionMessage(ComponentName admin);

    List<String> setMeteredDataDisabledPackages(ComponentName admin, List<String> packageNames);
    List<String> getMeteredDataDisabledPackages(ComponentName admin);

    int addOverrideApn(ComponentName admin, ApnSetting apnSetting);
    boolean updateOverrideApn(ComponentName admin, int apnId, ApnSetting apnSetting);
    boolean removeOverrideApn(ComponentName admin, int apnId);
    List<ApnSetting> getOverrideApns(ComponentName admin);
    void setOverrideApnsEnabled(ComponentName admin, boolean enabled);
    boolean isOverrideApnEnabled(ComponentName admin);

    boolean isMeteredDataDisabledPackageForUser(ComponentName admin, String packageName, int userId);

    int setGlobalPrivateDns(ComponentName admin, int mode, String privateDnsHost);
    int getGlobalPrivateDnsMode(ComponentName admin);
    String getGlobalPrivateDnsHost(ComponentName admin);

    void setProfileOwnerOnOrganizationOwnedDevice(ComponentName who, int userId, boolean isProfileOwnerOnOrganizationOwnedDevice);

//    void installUpdateFromFile(ComponentName admin, ParcelFileDescriptor updateFileDescriptor, StartInstallingUpdateCallback listener);

    void setCrossProfileCalendarPackages(ComponentName admin, List<String> packageNames);
    List<String> getCrossProfileCalendarPackages(ComponentName admin);
    boolean isPackageAllowedToAccessCalendarForUser(String packageName, int userHandle);
    List<String> getCrossProfileCalendarPackagesForUser(int userHandle);

    void setCrossProfilePackages(ComponentName admin, List<String> packageNames);
    List<String> getCrossProfilePackages(ComponentName admin);

    List<String> getAllCrossProfilePackages();
    List<String> getDefaultCrossProfilePackages();

    boolean isManagedKiosk();
    boolean isUnattendedManagedKiosk();

    boolean startViewCalendarEventInManagedProfile(String packageName, long eventId, long start, long end, boolean allDay, int flags);

    boolean setKeyGrantForApp(ComponentName admin, String callerPackage, String alias, String packageName, boolean hasGrant);
//    ParcelableGranteeMap getKeyPairGrants(String callerPackage, String alias);
    boolean setKeyGrantToWifiAuth(String callerPackage, String alias, boolean hasGrant);
    boolean isKeyPairGrantedToWifiAuth(String callerPackage, String alias);

    void setUserControlDisabledPackages(ComponentName admin, List<String> packages);

    List<String> getUserControlDisabledPackages(ComponentName admin);

    void setCommonCriteriaModeEnabled(ComponentName admin, boolean enabled);
    boolean isCommonCriteriaModeEnabled(ComponentName admin);

    int getPersonalAppsSuspendedReasons(ComponentName admin);
    void setPersonalAppsSuspended(ComponentName admin, boolean suspended);

    long getManagedProfileMaximumTimeOff(ComponentName admin);
    void setManagedProfileMaximumTimeOff(ComponentName admin, long timeoutMs);

    void acknowledgeDeviceCompliant();
    boolean isComplianceAcknowledgementRequired();

    boolean canProfileOwnerResetPasswordWhenLocked(int userId);

    void setNextOperationSafety(int operation, int reason);
    boolean isSafeOperation(int reason);

    String getEnrollmentSpecificId(String callerPackage);
    void setOrganizationIdForUser(String callerPackage, String enterpriseId, int userId);

//    UserHandle createAndProvisionManagedProfile(ManagedProfileProvisioningParams provisioningParams, String callerPackage);
//    void provisionFullyManagedDevice(FullyManagedDeviceProvisioningParams provisioningParams, String callerPackage);

    void finalizeWorkProfileProvisioning(UserHandle managedProfileUser, Account migratedAccount);

    void setDeviceOwnerType(ComponentName admin, int deviceOwnerType);
    int getDeviceOwnerType(ComponentName admin);

    void resetDefaultCrossProfileIntentFilters(int userId);
    boolean canAdminGrantSensorsPermissionsForUser(int userId);

    void setUsbDataSignalingEnabled(String callerPackage, boolean enabled);
    boolean isUsbDataSignalingEnabled(String callerPackage);
    boolean isUsbDataSignalingEnabledForUser(int userId);
    boolean canUsbDataSignalingBeDisabled();

    void setMinimumRequiredWifiSecurityLevel(int level);
    int getMinimumRequiredWifiSecurityLevel();

    void setWifiSsidPolicy(WifiSsidPolicy policy);
    WifiSsidPolicy getWifiSsidPolicy();

    List<UserHandle> listForegroundAffiliatedUsers();
    void setDrawables(List<DevicePolicyDrawableResource> drawables);
    void resetDrawables(List<String> drawableIds);
    ParcelableResource getDrawable(String drawableId, String drawableStyle, String drawableSource);

    boolean isDpcDownloaded();
    void setDpcDownloaded(boolean downloaded);

    void setStrings(List<DevicePolicyStringResource> strings);
    void resetStrings(List<String> stringIds);
    ParcelableResource getString(String stringId);

    boolean shouldAllowBypassingDevicePolicyManagementRoleQualification();

    List<UserHandle> getPolicyManagedProfiles(UserHandle userHandle);

    abstract class Stub extends Binder implements IDevicePolicyManager {
        public static IDevicePolicyManager asInterface(IBinder obj) {
            throw new UnsupportedOperationException();
        }
    }
}
