import SwiftUI
import VCF400Kit

/// VCFMAIN (F-01, L-01): AS/400 DEMO MENU
struct MainMenuView: View {
    @EnvironmentObject var model: AppModel
    @State private var option = ""
    @State private var profile = ""

    var body: some View {
        Screen(legacyPath: "GO VCFMAIN") {
            ScreenHeader(title: "AS/400 DEMO MENU", subtitle: "Welcome to... Vintage Computer Festival 2024", path: "VCFMAIN")
            if let m = model.message {
                Text(m).padding(12).frame(maxWidth: .infinity, alignment: .leading)
                    .background(Color.orange.opacity(0.15), in: RoundedRectangle(cornerRadius: 10))
                    .accessibilityIdentifier("msgbar")
            }
            LazyVGrid(columns: [GridItem(.adaptive(minimum: 300), spacing: 16)], alignment: .leading, spacing: 16) {
                group("Start Here") {
                    MenuItem(number: "1", title: "Learn AS/400 Navigation") { model.selectMain(option: "1") }
                    MenuItem(number: "2", title: "IBM Midrange History", enabled: false) {}
                    MenuItem(number: "3", title: "About This System", enabled: false) {}
                    MenuItem(number: "4", title: "About Gertie the System/34", enabled: false) {}
                }
                group("Exhibit Tasks") {
                    MenuItem(number: "11", title: "Nominate Exhibit for Award") { model.selectMain(option: "11") }
                    MenuItem(number: "12", title: "Sign Exhibit Guestbook") { model.selectMain(option: "12") }
                    MenuItem(number: "13", title: "Read a Guestbook Comment") { model.selectMain(option: "13") }
                }
                group("Office Tasks") {
                    MenuItem(number: "5", title: "Start OfficeVision/400", enabled: false) {}
                    MenuItem(number: "6", title: "Start a Calendar", enabled: false) {}
                    MenuItem(number: "7", title: "Start the Clock", enabled: false) {}
                }
                group("System Tasks") {
                    MenuItem(number: "80", title: "Sign Off the System") { model.selectMain(option: "80") }
                }
                group("Administration") {
                    MenuItem(number: "90", title: "Start Admin Menu", enabled: false) {}
                }
                group("Entertainment") {
                    MenuItem(number: "8", title: "Play Snake", enabled: false) {}
                    MenuItem(number: "9", title: "Play Yahtzee", enabled: false) {}
                    MenuItem(number: "10", title: "Play PacMan", enabled: false) {}
                }
            }
            Card {
                Text("Type the menu number you want and then press the ENTER key. Or select the option by tapping it.")
                    .font(.subheadline).foregroundStyle(.secondary)
                OptionLine(label: "Type Number, then press ENTER", option: $option, maxLength: 2) {
                    model.selectMain(option: option); option = ""
                }
            }
            DisclosureGroup("Sign on as a different user profile (kiosk exhibit / MM2024 shared terminal)") {
                HStack {
                    TextField("User profile", text: $profile).textInputAutocapitalization(.characters).autocorrectionDisabled()
                        .font(.system(.body, design: .monospaced)).padding(10)
                        .background(Color(.secondarySystemBackground), in: RoundedRectangle(cornerRadius: 10))
                        .frame(width: 200).accessibilityIdentifier("profile")
                    Button("Sign on") { model.signOn(profile) }.buttonStyle(.bordered).accessibilityIdentifier("signon")
                    Divider().frame(height: 24)
                    Text("Kiosk:").foregroundStyle(.secondary)
                    ForEach(model.repo.allExhibits(), id: \.exhusrprf) { e in
                        Button(e.exhusrprf) { model.startKiosk(e.exhusrprf) }.buttonStyle(.bordered)
                            .accessibilityIdentifier("kiosk.\(e.exhusrprf)")
                    }
                }
                .padding(.top, 8)
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
        Screen(legacyPath: "VCFMAIN → NTRSTIT") {
            ScreenHeader(title: "VCF/400 - How to Navigate", subtitle: "Before you start, here is how to navigate:", path: "NTRSTIT")
            Card {
                Text("1. Use ARROW KEYS or TAB KEY to move between fields.")
                Text("2. Use F5 or ENTER to perform your action.")
                Text("3. Use F12 to quit at any time.")
                Text("4. Keyboard not responding? If you see X II in the left corner, press RIGHT CTRL to continue.")
                Text("On this iPad UI every action button also shows its function key.").font(.footnote).foregroundStyle(.secondary)
            }
            FKeyBar(legend: "Got all that?") {
                FKeyButton(title: "続行", key: "ENTER", primary: true) { model.path.append(next) }
            }
        }
    }
}

struct SignoffView: View {
    @EnvironmentObject var model: AppModel
    var body: some View {
        Screen(legacyPath: "SIGNOFF") {
            Card {
                Text("Signed off").font(.largeTitle.bold()).foregroundStyle(Theme.brand)
                Text("Your session has ended. Thank you for visiting VCF/400.")
                Button("Sign on again") { model.signOn(model.defaultProfile) }.buttonStyle(.borderedProminent)
                    .accessibilityIdentifier("signon.again")
            }
        }
    }
}
