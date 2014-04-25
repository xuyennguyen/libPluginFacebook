/*
 * MyFacebookManager.h
 *
 *  Created on: Feb 13, 2014
 *      Author: Xuyen
 */

#ifndef FACEBOOKMANAGER_H_
#define FACEBOOKMANAGER_H_

#include "ProtocolSocial.h"

class MyShareResult : public cocos2d::plugin::ShareResultListener
{
public:
	void setListener( ShareResultListener *listener);
	virtual void onShareResult(cocos2d::plugin::ShareResultCode ret, const char* msg);
	virtual void onLoginResult(cocos2d::plugin::ShareResultCode ret, const char* msg);

private:
	ShareResultListener *m_pLister;
};

class FacebookManager
{
public:
	static FacebookManager* sharedSocialManager();
    static void purgeManager();
	void unloadSocialPlugin();
    void loadSocialPlugin();
    void shareByMode(cocos2d::plugin::TShareInfo info);
    void onResumeActivity();
    void setListener( cocos2d::plugin::ShareResultListener  *listener);

private:
    FacebookManager();
    virtual ~FacebookManager();

    static FacebookManager* s_pManager;

    cocos2d::plugin::ProtocolSocial* s_pFacebook;
    MyShareResult* s_pRetListener;

};

#endif /* MYFACEBOOKMANAGER_H_ */
