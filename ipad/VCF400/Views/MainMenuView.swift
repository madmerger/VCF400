import SwiftUI
import VCF400Kit

/// VCFMAIN (F-01, L-01): AS/400 DEMO MENU
struct MainMenuView: View {
    @EnvironmentObject var model: AppModel
    @State private var option = ""
    @State private var profile = ""

    var body: some View {
        Screen(screen: "VCFMAIN", legacyPath: "GO VCFMAIN") {
            ScreenHeader(title: "AS/400 DEMO MENU", subtitle: L10n.mainSubtitle, jaTitle: "VCF/400 デモメニュー", path: "VCFMAIN")
            if let m = model.message {
                Text(m).padding(12).frame(maxWidth: .infinity, alignment: .leading)
                    .background(Color.orange.opacity(0.15), in: RoundedRectangle(cornerRadius: 10))
                    .accessibilityIdentifier("msgbar")
            }
            LazyVGrid(columns: [GridItem(.adaptive(minimum: 300), spacing: 16)], alignment: .leading, spacing: 16) {
                group(L10n.groupStart) {
                    MenuItem(number: "1", title: L10n.learnNavigation) { model.selectMain(option: "1") }
                    MenuItem(number: "2", title: L10n.history, enabled: false) {}
                    MenuItem(number: "3", title: L10n.aboutSystem, enabled: false) {}
                    MenuItem(number: "4", title: L10n.aboutGertie, enabled: false) {}
                }
                group(L10n.groupExhibit) {
                    MenuItem(number: "11", title: L10n.nominate) { model.selectMain(option: "11") }
                    MenuItem(number: "12", title: L10n.signGuestbook) { model.selectMain(option: "12") }
                    MenuItem(number: "13", title: L10n.readGuestbook) { model.selectMain(option: "13") }
                }
                group(L10n.groupOffice) {
                    MenuItem(number: "5", title: L10n.officeVision, enabled: false) {}
                    MenuItem(number: "6", title: L10n.calendar, enabled: false) {}
                    MenuItem(number: "7", title: L10n.clock, enabled: false) {}
                }
                group(L10n.groupSystem) {
                    MenuItem(number: "80", title: L10n.signOffSystem) { model.selectMain(option: "80") }
                }
                group(L10n.groupAdmin) {
                    MenuItem(number: "90", title: L10n.adminMenu, enabled: false) {}
                }
                group(L10n.groupEntertainment) {
                    MenuItem(number: "8", title: L10n.snake, enabled: false) {}
                    MenuItem(number: "9", title: L10n.yahtzee, enabled: false) {}
                    MenuItem(number: "10", title: L10n.pacman, enabled: false) {}
                }
            }
            Card {
                Text(L10n.mainGuidance)
                    .font(.subheadline).foregroundStyle(.secondary)
                OptionLine(label: L10n.optionPrompt, option: $option, maxLength: 2) {
                    model.selectMain(option: option); option = ""
                }
            }
            DisclosureGroup {
                HStack {
                    TextField(L10n.profilePlaceholder, text: $profile).textInputAutocapitalization(.characters).autocorrectionDisabled()
                        .font(.system(.body, design: .monospaced)).padding(10)
                        .background(Color(.secondarySystemBackground), in: RoundedRectangle(cornerRadius: 10))
                        .frame(width: 200).accessibilityIdentifier("profile")
                    Button(L10n.signOn) { model.signOn(profile) }.buttonStyle(.bordered).accessibilityIdentifier("signon")
                    Divider().frame(height: 24)
                    Text(L10n.kiosk).foregroundStyle(.secondary)
                    ForEach(model.repo.allExhibits(), id: \.exhusrprf) { e in
                        Button(e.exhusrprf) { model.startKiosk(e.exhusrprf) }.buttonStyle(.bordered)
                            .accessibilityIdentifier("kiosk.\(e.exhusrprf)")
                    }
                }
                .padding(.top, 8)
            } label: {
                Text(L10n.signonDisclosure).accessibilityIdentifier("signon.toggle")
            }
            .font(.subheadline).foregroundStyle(.secondary)
            .padding(16).background(Theme.card, in: RoundedRectangle(cornerRadius: 14))
        }
    }

    private func group<C: View>(_ title: String, @ViewBuilder _ items: () -> C) -> some View {
        Card {
            Text(title).font(.caption.bold()).tracking(2).textCase(.uppercase).foregroundStyle(.secondary)
            items()
        }
    }
}

/// NTRSTIT (F-02, L-02): VCF/400 - How to Navigate
struct NavigateView: View {
    @EnvironmentObject var model: AppModel
    let next: Route
    var body: some View {
        Screen(screen: "NTRSTIT", legacyPath: "VCFMAIN → NTRSTIT") {
            ScreenHeader(title: "VCF/400 - How to Navigate", subtitle: L10n.navigationSubtitle, jaTitle: "VCF/400 の操作方法", path: "NTRSTIT")
            Card {
                Text(L10n.navigation1)
                Text(L10n.navigation2)
                Text(L10n.navigation3)
                Text(L10n.navigation4)
                Text(L10n.navigationFootnote).font(.footnote).foregroundStyle(.secondary)
            }
            FKeyBar(legend: L10n.navigationReady) {
                FKeyButton(title: "続行", key: "ENTER", primary: true) { model.path.append(next) }
            }
        }
    }
}

struct SignoffView: View {
    @EnvironmentObject var model: AppModel
    var body: some View {
        Screen(screen: "SIGNOFF", legacyPath: "SIGNOFF") {
            Card {
                Text(L10n.signedOff).font(.largeTitle.bold()).foregroundStyle(Theme.brand)
                Text(L10n.sessionEnded)
                Button(L10n.signOnAgain) { model.signOn(model.defaultProfile) }.buttonStyle(.borderedProminent)
                    .accessibilityIdentifier("signon.again")
            }
        }
    }
}
