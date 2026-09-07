import SwiftUI
import VCF400Kit

/// ADDGBCMT ADDCMT (F-04, L-07): GUESTBOOK/400 - ADD COMMENT。入力順: 名前 → 展示 ID → コメント
struct AddCommentView: View {
    @EnvironmentObject var model: AppModel
    @State private var inName = ""
    @State private var inId = ""
    @State private var inCmt = ""
    @State private var result: GuestbookService.AddResult?

    var body: some View {
        Screen(screen: "ADDCMT", legacyPath: "VCFMAIN → 12 → ADDGBSTUB → ADDGBCMT (GUESTBKSCR/ADDCMT)") {
            ScreenHeader(title: "GUESTBOOK/400 - ADD COMMENT", subtitle: L10n.voteSubtitle, jaTitle: "ゲストブックにコメントを追加", path: "ADDGBCMT / ADDCMT")
            Card {
                FieldRow(step: 1, label: L10n.name, hint: L10n.upTo16, error: result?.nameErr ?? false) {
                    TextField("", text: $inName).autocorrectionDisabled().accessibilityIdentifier("inName")
                        .onChange(of: inName) { _, v in if v.count > 16 { inName = String(v.prefix(16)) } }
                }
                FieldRow(step: 2, label: L10n.exhibitID,
                         hint: model.exhibitProtected ? L10n.fixedToExhibit : L10n.sharedTypeExhibit,
                         error: result?.exhibitErr ?? false) {
                    TextField("", text: $inId).textInputAutocapitalization(.characters).autocorrectionDisabled()
                        .font(.system(.body, design: .monospaced)).disabled(model.exhibitProtected)
                        .foregroundStyle(model.exhibitProtected ? .secondary : .primary).accessibilityIdentifier("inId")
                        .onChange(of: inId) { _, v in if v.count > 9 { inId = String(v.prefix(9)) } }
                }
                FieldRow(step: 3, label: L10n.comment, hint: L10n.upTo200, error: result?.commentErr ?? false) {
                    TextField("", text: $inCmt, axis: .vertical).lineLimit(3...6).accessibilityIdentifier("inCmt")
                        .onChange(of: inCmt) { _, v in if v.count > 200 { inCmt = String(v.prefix(200)) } }
                }
                ErrorLine(text: result?.errLine)
            }
            FKeyBar {
                FKeyButton(title: "キャンセル", key: "F12", destructive: true) { model.returnToCaller() }
                FKeyButton(title: "送信", key: "F5", primary: true) {
                    let r = model.guestbook.add(model.launch, name: inName, exhibit: inId, comment: inCmt)
                    if let c = r.added { model.path.append(.endComment(id: c.cmtid)) } else { result = r }
                }
            }
        }
        .onAppear { inId = model.guestbook.protectedExhibit(model.launch) }
    }
}

/// ENDCMT (L-08)
struct EndCommentView: View {
    @EnvironmentObject var model: AppModel
    let id: Int
    var body: some View {
        Screen(screen: "ENDCMT", legacyPath: "ADDGBCMT (GUESTBKSCR/ENDCMT)") {
            Card {
                Text("THANKS FOR COMMENTING!").font(.system(size: 34, weight: .black)).foregroundStyle(Theme.brand).accessibilityIdentifier("screenTitle")
                Text(L10n.commentEndJa).font(.title3).foregroundStyle(.secondary)
                HStack(spacing: 6) {
                    Text(L10n.commentThanks)
                    Text("\(id)").monospaced().bold().accessibilityIdentifier("out.cmtid")
                }
            }
            FKeyBar(legend: "ENTER を押して終了します。") {
                FKeyButton(title: "終了", key: "ENTER", primary: true) { model.returnToCaller() }
            }
        }
    }
}

/// READGBCMT READCMT (F-05, L-09): GUESTBOOK/400 - Read a Comment
struct ReadCommentView: View {
    @EnvironmentObject var model: AppModel
    @State private var inCmtId = ""
    @State private var errLine: String?
    @State private var out: GuestbookService.ReadResult?

    var body: some View {
        Screen(screen: "READCMT", legacyPath: "VCFMAIN → 13 → READGBSTUB → READGBCMT (GUESTBKSCR/READCMT)") {
            ScreenHeader(title: "GUESTBOOK/400 - Read a Comment", subtitle: L10n.voteSubtitle, jaTitle: "ゲストブックのコメントを読む", path: "READGBCMT / READCMT")
            Card {
                FieldRow(step: 1, label: L10n.enterCommentID,
                         hint: nil, error: errLine != nil) {
                    TextField("", text: $inCmtId).keyboardType(.numberPad).font(.system(.body, design: .monospaced))
                        .onSubmit(submit).accessibilityIdentifier("inCmtId")
                        .onChange(of: inCmtId) { _, v in if v.count > 4 { inCmtId = String(v.prefix(4)) } }
                }
                HStack(spacing: 0) {
                    Text(L10n.currentComments)
                    Text("\(model.guestbook.totalComments)").accessibilityIdentifier("out.total")
                }.font(.caption).foregroundStyle(.secondary)
                ErrorLine(text: errLine)
            }
            if let o = out {
                Card {
                    HStack(spacing: 6) {
                        Text(o.outName).bold().accessibilityIdentifier("out.name")
                        Text(L10n.saysTo).foregroundStyle(.secondary)
                        Text(o.outTitle).bold().accessibilityIdentifier("out.title")
                    }
                    Text(o.outCmt).accessibilityIdentifier("out.cmt")
                    Text("\(L10n.commentID) \(o.record.cmtid)").font(.caption).foregroundStyle(.secondary).monospaced().accessibilityIdentifier("out.cmtid")
                }
            }
            Text(L10n.reportComment)
                .font(.footnote).foregroundStyle(.secondary)
            FKeyBar {
                FKeyButton(title: "キャンセル", key: "F12", destructive: true) { model.returnToCaller() }
                FKeyButton(title: "送信", key: "F5", primary: true, action: submit)
            }
        }
    }

    private func submit() {
        let id = numeric(inCmtId, digits: 4)
        if id == 0 { errLine = Messages.errcmtid; out = nil; return }                 // V-14
        errLine = nil
        out = model.guestbook.read(model.launch, id: id)
    }
}
