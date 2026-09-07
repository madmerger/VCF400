import SwiftUI
import VCF400Kit

/// ADDVOTE VOTE1 (F-03, L-03): NOMINATE EXHIBIT FOR AWARD。入力順: バッジ → 展示 ID → アワード ID
struct VoteView: View {
    @EnvironmentObject var model: AppModel
    @State private var inputBadge = ""
    @State private var inExhb = ""
    @State private var inputAward = ""
    @State private var result: VoteService.Result?
    @FocusState private var focus: Field?
    enum Field { case badge, exhb, award }

    var body: some View {
        Screen(screen: "VOTE1", legacyPath: "VCFMAIN → 11 → VOTESTUB → ADDVOTE (VOTESCR/VOTE1)") {
            ScreenHeader(title: "NOMINATE EXHIBIT FOR AWARD", subtitle: L10n.voteSubtitle, jaTitle: "展示をアワードに推薦", path: "ADDVOTE / VOTE1")
            Card {
                FieldRow(step: 1, label: L10n.badgeLabel, hint: L10n.digits4, error: result?.badgeErr ?? false) {
                    TextField("", text: $inputBadge).keyboardType(.numberPad).font(.system(.body, design: .monospaced))
                        .focused($focus, equals: .badge).onSubmit(submit).accessibilityIdentifier("inputBadge")
                        .onChange(of: inputBadge) { _, v in if v.count > 4 { inputBadge = String(v.prefix(4)) } }
                }
                FieldRow(step: 2, label: L10n.exhibitLabel,
                         hint: model.exhibitProtected ? L10n.fixedExhibit : L10n.sharedExhibit,
                         error: result?.exhibitErr ?? false) {
                    TextField("", text: $inExhb).textInputAutocapitalization(.characters).autocorrectionDisabled()
                        .font(.system(.body, design: .monospaced)).disabled(model.exhibitProtected)
                        .foregroundStyle(model.exhibitProtected ? .secondary : .primary)
                        .focused($focus, equals: .exhb).onSubmit(submit).accessibilityIdentifier("inExhb")
                        .onChange(of: inExhb) { _, v in if v.count > 9 { inExhb = String(v.prefix(9)) } }
                }
                FieldRow(step: 3, label: L10n.awardLabel, hint: L10n.digits3, error: result?.awardErr ?? false) {
                    TextField("", text: $inputAward).keyboardType(.numberPad).font(.system(.body, design: .monospaced))
                        .focused($focus, equals: .award).onSubmit(submit).accessibilityIdentifier("inputAward")
                        .onChange(of: inputAward) { _, v in if v.count > 3 { inputAward = String(v.prefix(3)) } }
                }
                ErrorLine(text: result?.errLine)
            }
            Card {
                Text(L10n.availableAwards).font(.headline)
                ForEach(model.votes.availableAwards, id: \.awardid) { a in
                    HStack(alignment: .top, spacing: 12) {
                        Text("\(a.number).").font(.system(.body, design: .monospaced).bold()).foregroundStyle(Theme.brand)
                        VStack(alignment: .leading) {
                            Text(a.awardtitle).fontWeight(.bold)
                            Text("- \(a.awarddesc)").font(.subheadline).foregroundStyle(.secondary)
                        }
                    }
                    .padding(10).frame(maxWidth: .infinity, alignment: .leading)
                    .background(Color(.secondarySystemBackground), in: RoundedRectangle(cornerRadius: 10))
                }
                Text(L10n.oneAward).foregroundStyle(Theme.danger).fontWeight(.semibold)
            }
            FKeyBar(legend: L10n.tabSubmit) {
                FKeyButton(title: "キャンセル", key: "F12", destructive: true) { model.returnToCaller() }
                    .keyboardShortcut(.escape, modifiers: [])
                FKeyButton(title: "送信", key: "F5", primary: true, action: submit)
            }
        }
        .onAppear {
            inExhb = model.votes.protectedExhibit(model.launch)
            if !model.votes.isVotingAllowed {                                   // B-03 ENDOFCON
                model.path.removeLast(); model.path.append(.endOfCon)
            }
        }
    }

    private func submit() {
        let r = model.votes.submit(model.launch, badge: numeric(inputBadge, digits: 4), exhibit: inExhb, award: numeric(inputAward, digits: 3))
        if r.recorded, let v = r.vote {
            model.path.append(.voteEnd(badge: v.badgenbr, exhibit: v.exhbnbr, award: v.awardnbr))
        } else {
            result = r
        }
    }
}

/// VOTEEND (L-04)
struct VoteEndView: View {
    @EnvironmentObject var model: AppModel
    let badge: Int, exhibit: String, award: Int
    var body: some View {
        Screen(screen: "VOTEEND", legacyPath: "ADDVOTE (VOTESCR/VOTEEND)") {
            Card {
                Text("THANK YOU FOR VOTING!").font(.system(size: 34, weight: .black)).foregroundStyle(Theme.brand)
                    .accessibilityIdentifier("screenTitle")
                Text(L10n.voteEndJa).font(.title3).foregroundStyle(.secondary)
                Text(L10n.recorded).fontWeight(.bold).foregroundStyle(Theme.ok)
                Text(L10n.voteThanks)
                Grid(alignment: .leading) {
                    GridRow { Text(L10n.badge).foregroundStyle(.secondary); Text("\(badge)").monospaced().accessibilityIdentifier("out.badge") }
                    GridRow { Text(L10n.exhibit).foregroundStyle(.secondary); Text(exhibit).monospaced().accessibilityIdentifier("out.exhibit") }
                    GridRow { Text(L10n.award).foregroundStyle(.secondary); Text(String(format: "%03d", award)).monospaced().accessibilityIdentifier("out.award") }
                }
            }
            FKeyBar(legend: L10n.voteEndFootnote) {
                FKeyButton(title: "メニューへ戻る", key: "ENTER", primary: true) { model.returnToCaller() }
            }
        }
    }
}

/// ENDOFCON (B-03): 投票期間終了
struct EndOfConView: View {
    @EnvironmentObject var model: AppModel
    var body: some View {
        Screen(screen: "ENDOFCON", legacyPath: "ADDVOTE (VOTESCR/ENDOFCON)") {
            Card {
                Text("SORRY!").font(.system(size: 34, weight: .black)).foregroundStyle(Theme.danger).accessibilityIdentifier("screenTitle")
                Text(L10n.endOfConJa).font(.title3).foregroundStyle(.secondary)
                Text(L10n.votingEnded).fontWeight(.bold)
                Text(L10n.guestbookInstead)
            }
            FKeyBar(legend: "ENTER を押して終了します。") {
                FKeyButton(title: "終了", key: "ENTER", primary: true) { model.returnToCaller() }
            }
        }
    }
}
