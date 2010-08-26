/**
 * Copyright (C) 2010 Regis Montoya (aka r3gis - www.r3gis.fr)
 * This file is part of CSipSimple.
 *
 *  CSipSimple is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  CSipSimple is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with CSipSimple.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.csipsimple.wizards.impl;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.pjsip.pjsua.pj_str_t;
import org.pjsip.pjsua.pjsip_cred_data_type;
import org.pjsip.pjsua.pjsip_cred_info;
import org.pjsip.pjsua.pjsua;

import android.preference.EditTextPreference;
import com.csipsimple.utils.Log;

import com.csipsimple.R;
import com.csipsimple.wizards.BasePrefsWizard;
import com.csipsimple.wizards.WizardUtils.WizardInfo;

public class Basic extends BasePrefsWizard {
	protected static final String THIS_FILE = "Basic W";
	
	public static WizardInfo getWizardInfo() {
		WizardInfo result = new WizardInfo();
		result.id =  "BASIC";
		result.label = "Basic";
		result.icon = R.drawable.ic_wizard_basic;
		result.priority = 3;
		result.countries = new Locale[]{};
		result.isGeneric = true;
		return result;
	}

	private EditTextPreference accountDisplayName;
	private EditTextPreference accountUserName;
	private EditTextPreference accountServer;
	private EditTextPreference accountPassword;

	
	protected void fillLayout() {
		accountDisplayName = (EditTextPreference) findPreference("display_name");
		accountUserName = (EditTextPreference) findPreference("username");
		accountServer = (EditTextPreference) findPreference("server");
		accountPassword = (EditTextPreference) findPreference("password");

		
		
		accountDisplayName.setText(account.display_name);
		String server = "";
		String account_cfgid = account.cfg.getId().getPtr();
		if(account_cfgid == null) {
			account_cfgid = "";
		}
		Pattern p = Pattern.compile("[^<]*<sip:([^@]*)@([^>]*)>");
		Matcher m = p.matcher(account_cfgid);

		if (m.matches()) {
			account_cfgid = m.group(1);
			server = m.group(2);
		}
		

		accountUserName.setText(account_cfgid);
		accountServer.setText(server);
		
		pjsip_cred_info ci = account.cfg.getCred_info();
		accountPassword.setText(ci.getData().getPtr());
	}

	protected void updateDescriptions() {
		setStringFieldSummary("display_name");
		setStringFieldSummary("username");
		setStringFieldSummary("server");
		setPasswordFieldSummary("password");
	}

	protected boolean canSave() {
		boolean isValid = true;
		
		isValid &= checkField(accountDisplayName, isEmpty(accountDisplayName));
		isValid &= checkField(accountPassword, isEmpty(accountPassword));
		isValid &= checkField(accountServer, isEmpty(accountServer));
		isValid &= checkField(accountUserName, isEmpty(accountUserName));

		return isValid;
	}

	protected void buildAccount() {
		Log.d(THIS_FILE, "begin of save ....");
		account.display_name = accountDisplayName.getText();
		// TODO add an user display name
		account.cfg.setId(pjsua.pj_str_copy("<sip:"
				+ accountUserName.getText() + "@"+accountServer.getText()+">"));
		
		pj_str_t regUri = pjsua.pj_str_copy("sip:"+accountServer.getText());
		account.cfg.setReg_uri(regUri);
		account.cfg.setProxy_cnt(1);
		account.cfg.setProxy(regUri);

		pjsip_cred_info ci = account.cfg.getCred_info();

		account.cfg.setCred_count(1);
		ci.setRealm(pjsua.pj_str_copy("*"));
		ci.setUsername(getPjText(accountUserName));
		ci.setData(getPjText(accountPassword));
		ci.setScheme(pjsua.pj_str_copy("Digest"));
		ci.setData_type(pjsip_cred_data_type.PJSIP_CRED_DATA_PLAIN_PASSWD
				.swigValue());

	}

	@Override
	protected String getWizardId() {
		return getWizardInfo().id;
	}

	@Override
	protected int getXmlPreferences() {
		return R.xml.w_basic_preferences;
	}

	@Override
	protected String getXmlPrefix() {
		return "basic";
	}
}
