# Header
Name: agility-adapters-core-azure
Summary: Agility - Core Functionality for Azure Adapters
Version: %rpm_version
Release: %rpm_revision
Vendor: CSC Agility Dev
URL: http://www.csc.com/
Group: Services/Cloud
License: Commercial
BuildArch: noarch
Requires: jre >= 1.7.0
Requires: agility-platform-common

# Sections
%description
Core functionality for Azure adapters utilized with the Agility Platform.

%license_text

%files
%defattr(644,smadmin,smadmin,755)
/opt/agility-platform/deploy/*
