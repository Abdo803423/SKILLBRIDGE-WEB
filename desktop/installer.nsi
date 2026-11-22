!include "MUI2.nsh"

Name "SkillBridge"
OutFile "dist/SkillBridge-Windows-Setup.exe"
InstallDir "$LOCALAPPDATA\Programs\SkillBridge"
InstallDirRegKey HKCU "Software\SkillBridge" "InstallDir"
RequestExecutionLevel user
SetCompressor /SOLID zlib

!define MUI_ABORTWARNING
!define MUI_ICON "build/icon.ico"
!define MUI_UNICON "build/icon.ico"
!define MUI_FINISHPAGE_RUN "$INSTDIR\SkillBridge.exe"
!define MUI_FINISHPAGE_RUN_TEXT "Launch SkillBridge Desktop"

!insertmacro MUI_PAGE_WELCOME
!insertmacro MUI_PAGE_DIRECTORY
!insertmacro MUI_PAGE_INSTFILES
!insertmacro MUI_PAGE_FINISH

!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_INSTFILES

!insertmacro MUI_LANGUAGE "English"

Section "SkillBridge Desktop Application" SecMain
  SetOutPath "$INSTDIR"
  File /r "dist/win-unpacked/*"

  WriteRegStr HKCU "Software\SkillBridge" "InstallDir" "$INSTDIR"
  WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\SkillBridge" "DisplayName" "SkillBridge"
  WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\SkillBridge" "DisplayVersion" "1.0.0"
  WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\SkillBridge" "Publisher" "SkillBridge Ecosystem"
  WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\SkillBridge" "DisplayIcon" "$INSTDIR\SkillBridge.exe"
  WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\SkillBridge" "UninstallString" '"$INSTDIR\Uninstall SkillBridge.exe"'
  WriteRegDWORD HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\SkillBridge" "NoModify" 1
  WriteRegDWORD HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\SkillBridge" "NoRepair" 1

  WriteUninstaller "$INSTDIR\Uninstall SkillBridge.exe"

  CreateDirectory "$SMPROGRAMS\SkillBridge"
  CreateShortcut "$SMPROGRAMS\SkillBridge\SkillBridge.lnk" "$INSTDIR\SkillBridge.exe" "" "$INSTDIR\SkillBridge.exe" 0
  CreateShortcut "$SMPROGRAMS\SkillBridge\Uninstall SkillBridge.lnk" "$INSTDIR\Uninstall SkillBridge.exe"
  CreateShortcut "$DESKTOP\SkillBridge.lnk" "$INSTDIR\SkillBridge.exe" "" "$INSTDIR\SkillBridge.exe" 0
SectionEnd

Section "Uninstall"
  Delete "$DESKTOP\SkillBridge.lnk"
  Delete "$SMPROGRAMS\SkillBridge\SkillBridge.lnk"
  Delete "$SMPROGRAMS\SkillBridge\Uninstall SkillBridge.lnk"
  RMDir "$SMPROGRAMS\SkillBridge"

  RMDir /r "$INSTDIR"
  DeleteRegKey HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\SkillBridge"
  DeleteRegKey HKCU "Software\SkillBridge"
SectionEnd
