import SwiftUI
import VCF400Kit

/// LRN400 (F-06, L-10): LEARN/400。F3 Exit / F5 Forwards / F8 Backwards
struct LearnView: View {
    @EnvironmentObject var model: AppModel
    @State private var state: LearnService.State?

    var body: some View {
        Screen(screen: "LRN400", legacyPath: "VCFMAIN → 1 → LRN400 (LRN400SCR/MAIN)") {
            HStack(alignment: .firstTextBaseline) {
                ScreenHeader(title: "LEARN/400", jaTitle: "LEARN/400 学習", path: "LRN400 / MAIN")
                Spacer()
                HStack(spacing: 4) {
                    Text(L10n.learnPage).foregroundStyle(.secondary)
                    Text(state?.outPageNbr ?? "").bold().accessibilityIdentifier("out.page")
                }
            }
            .accessibilityElement(children: .contain)
            Card {
                Text(state?.outContent ?? "").font(.title3).lineSpacing(6).frame(minHeight: 180, alignment: .topLeading)
                    .accessibilityIdentifier("out.content")
            }
            FKeyBar(legend: L10n.learnLegend) {
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
        Screen(screen: "KIOSK", legacyPath: "STREXHB EXHBNAME(\(exhibitId)) → EXHBMENU (EXHBMENUSC/MENU)") {
            if let e = model.kiosk.exhibit(for: exhibitId) {
                ScreenHeader(title: "WELCOME TO...", subtitle: L10n.kioskSubtitle, jaTitle: "\(e.exhbtitle) へようこそ", path: "EXHBMENU / MENU")
                Text(L10n.hostedBy).foregroundStyle(.secondary) + Text(e.exhbitor).bold() + Text(" (\(e.exhbcity) \(e.exhbstate))").foregroundStyle(.secondary)
                Text(e.exhbtitle).font(.title2.bold())
                if let m = model.message {
                    Text(m).padding(12).frame(maxWidth: .infinity, alignment: .leading)
                        .background(Color.orange.opacity(0.15), in: RoundedRectangle(cornerRadius: 10)).accessibilityIdentifier("msgbar")
                }
                Card { Text(e.exhbdesc) }
                Card {
                    if e.isEligible { MenuItem(number: "1", title: L10n.kioskMenu1) { model.selectKiosk(e, option: "1") } }
                    if e.isLearnEnabled { MenuItem(number: "2", title: L10n.kioskMenu2) { model.selectKiosk(e, option: "2") } }
                    MenuItem(number: "3", title: L10n.kioskMenu3) { model.selectKiosk(e, option: "3") }
                    MenuItem(number: "4", title: L10n.kioskMenu4) { model.selectKiosk(e, option: "4") }
                    OptionLine(label: L10n.kioskOption, option: $option, maxLength: 1) {
                        model.selectKiosk(e, option: option); option = ""
                    }
                }
            } else {
                Text("\(L10n.notFound) \(exhibitId) が見つかりません。")
            }
        }
    }
}

/// ADMPSWRD (L-06): キオスク終了パスワード
struct AdmPswrdView: View {
    @EnvironmentObject var model: AppModel
    let exhibitId: String
    @State private var inPwd = ""
    @State private var failures = 0
    @State private var lockedUntil: Date?

    private var isLocked: Bool {
        (lockedUntil ?? model.kioskPasswordLockedUntil).map { $0 > Date() } ?? false
    }

    private func submit() {
        guard !isLocked else { return }
        if model.kiosk.exitAllowed(inPwd) {
            model.resetKioskPasswordThrottle()
            model.exitKiosk(exhibitId, password: inPwd)
        } else {
            model.recordKioskPasswordFailure()
            failures = model.kioskPasswordFailures
            lockedUntil = model.kioskPasswordLockedUntil
            model.exitKiosk(exhibitId, password: inPwd)
        }
    }

    var body: some View {
        Screen(screen: "ADMPSWRD", legacyPath: "EXHBMENU (EXHBMENUSC/ADMPSWRD)") {
            ScreenHeader(title: L10n.adminTitle, subtitle: L10n.adminSubtitle, jaTitle: L10n.adminOriginal, path: "EXHBMENU / ADMPSWRD")
            if isLocked {
                HStack(spacing: 10) {
                    Image(systemName: "exclamationmark.circle.fill")
                    Text(L10n.pwLocked).fontWeight(.semibold)
                }
                .foregroundStyle(Theme.danger)
                .padding(12)
                .frame(maxWidth: .infinity, alignment: .leading)
                .background(Theme.dangerBg, in: RoundedRectangle(cornerRadius: 10))
                .accessibilityIdentifier("lockline")
            }
            Card {
                FieldRow(step: 1, label: L10n.adminPassword) {
                    SecureField("", text: $inPwd).font(.system(.body, design: .monospaced)).accessibilityIdentifier("inPwd")
                        .disabled(isLocked)
                        .onSubmit(submit)
                }
            }
            FKeyBar {
                FKeyButton(title: "メニューへ戻る", key: "F12") { model.returnToCaller() }
                FKeyButton(title: "サインオフ", key: "ENTER", primary: true, action: submit)
                    .disabled(isLocked)
            }
        }
        .onAppear {
            failures = model.kioskPasswordFailures
            lockedUntil = model.kioskPasswordLockedUntil
        }
        .task(id: lockedUntil) {
            guard let until = lockedUntil else { return }
            let delay = max(0, until.timeIntervalSinceNow)
            try? await Task.sleep(nanoseconds: UInt64(delay * 1_000_000_000))
            guard !Task.isCancelled else { return }
            lockedUntil = nil
            model.kioskPasswordLockedUntil = nil
        }
    }
}
