import SwiftUI
import VCF400Kit

/// LRN400 (F-06, L-10): LEARN/400。F3 Exit / F5 Forwards / F8 Backwards
struct LearnView: View {
    @EnvironmentObject var model: AppModel
    @State private var state: LearnService.State?

    var body: some View {
        Screen(legacyPath: "VCFMAIN → 1 → LRN400 (LRN400SCR/MAIN)") {
            HStack(alignment: .firstTextBaseline) {
                ScreenHeader(title: "LEARN/400", path: "LRN400 / MAIN")
                Spacer()
                Text("Page ").foregroundStyle(.secondary) + Text(state?.outPageNbr ?? "").bold()
            }
            .accessibilityElement(children: .contain)
            Text(state?.outPageNbr ?? "").hidden().frame(height: 0).accessibilityIdentifier("out.page")
            Card {
                Text(state?.outContent ?? "").font(.title3).lineSpacing(6).frame(minHeight: 180, alignment: .topLeading)
                    .accessibilityIdentifier("out.content")
            }
            FKeyBar(legend: "Cmd3/F3 = Exit · Cmd5/F5 = Forwards · Cmd8/F8 = Backwards") {
                FKeyButton(title: "戻る", key: "F3") { model.returnToCaller() }
                FKeyButton(title: "前へ", key: "F8") { if let s = state { state = model.learn.back(s) } }
                FKeyButton(title: "進む", key: "F5", primary: true) {
                    guard let s = state else { return }
                    let n = model.learn.forward(s)
                    if n.exit { model.returnToCaller() } else { state = n }        // EXTRA='END' (B-10)
                }
            }
        }
        .onAppear { if state == nil { state = model.learn.start() } }
    }
}

/// EXHBMENU (F-07, L-05): WELCOME TO... <exhibit>。1/2 は ELIGIBLE/ENLRN400 で非表示、7 は隠しオプション
struct KioskView: View {
    @EnvironmentObject var model: AppModel
    let exhibitId: String
    @State private var option = ""

    var body: some View {
        Screen(legacyPath: "STREXHB EXHBNAME(\(exhibitId)) → EXHBMENU (EXHBMENUSC/MENU)") {
            if let e = model.kiosk.exhibit(for: exhibitId) {
                ScreenHeader(title: e.exhbtitle, subtitle: "WELCOME TO...", path: "EXHBMENU / MENU")
                Text("HOSTED BY ").foregroundStyle(.secondary) + Text(e.exhbitor).bold() + Text(" OF \(e.exhbcity) \(e.exhbstate)").foregroundStyle(.secondary)
                if let m = model.message {
                    Text(m).padding(12).frame(maxWidth: .infinity, alignment: .leading)
                        .background(Color.orange.opacity(0.15), in: RoundedRectangle(cornerRadius: 10)).accessibilityIdentifier("msgbar")
                }
                Card { Text(e.exhbdesc) }
                Card {
                    if e.isEligible { MenuItem(number: "1", title: "Nominate This Exhibit for Award") { model.selectKiosk(e, option: "1") } }
                    if e.isLearnEnabled { MenuItem(number: "2", title: "Learn More About This Exhibit") { model.selectKiosk(e, option: "2") } }
                    MenuItem(number: "3", title: "Sign Exhibit Guestbook") { model.selectKiosk(e, option: "3") }
                    MenuItem(number: "4", title: "Read Exhibit Guestbook") { model.selectKiosk(e, option: "4") }
                    OptionLine(label: "Select Menu Option, Press ENTER:", option: $option, maxLength: 1) {
                        model.selectKiosk(e, option: option); option = ""
                    }
                }
            } else {
                Text("Exhibit \(exhibitId) not found.")
            }
        }
    }
}

/// ADMPSWRD (L-06): キオスク終了パスワード
struct AdmPswrdView: View {
    @EnvironmentObject var model: AppModel
    let exhibitId: String
    @State private var inPwd = ""
    var body: some View {
        Screen(legacyPath: "EXHBMENU (EXHBMENUSC/ADMPSWRD)") {
            ScreenHeader(title: "Are you sure you want to exit the kiosk?", subtitle: "Type the Administrator password, press ENTER to sign off", path: "EXHBMENU / ADMPSWRD")
            Card {
                FieldRow(step: 1, label: "Administrator password") {
                    SecureField("", text: $inPwd).font(.system(.body, design: .monospaced)).accessibilityIdentifier("inPwd")
                        .onSubmit { model.exitKiosk(exhibitId, password: inPwd) }
                }
            }
            FKeyBar {
                FKeyButton(title: "メニューへ戻る", key: "F12") { model.returnToCaller() }
                FKeyButton(title: "サインオフ", key: "ENTER", primary: true) { model.exitKiosk(exhibitId, password: inPwd) }
            }
        }
    }
}
