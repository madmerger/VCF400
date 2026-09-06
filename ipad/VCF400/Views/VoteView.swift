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
        Screen(legacyPath: "VCFMAIN → 11 → VOTESTUB → ADDVOTE (VOTESCR/VOTE1)") {
            ScreenHeader(title: "NOMINATE EXHIBIT FOR AWARD", subtitle: "Vintage Computer Festival", path: "ADDVOTE / VOTE1")
            Card {
                FieldRow(step: 1, label: "First, type your SFGE Badge Number", hint: "4 digits", error: result?.badgeErr ?? false) {
                    TextField("", text: $inputBadge).keyboardType(.numberPad).font(.system(.body, design: .monospaced))
                        .focused($focus, equals: .badge).onSubmit(submit).accessibilityIdentifier("inputBadge")
                        .onChange(of: inputBadge) { _, v in if v.count > 4 { inputBadge = String(v.prefix(4)) } }
                }
                FieldRow(step: 2, label: "Second, type the Exhibit ID you are nominating",
                         hint: model.exhibitProtected ? "Fixed to this exhibit (signed-on profile). Sign on as MM2024 to enter any exhibit."
                                                      : "Shared terminal (MM2024): type the exhibit's user profile, e.g. ASHIBATA",
                         error: result?.exhibitErr ?? false) {
                    TextField("", text: $inExhb).textInputAutocapitalization(.characters).autocorrectionDisabled()
                        .font(.system(.body, design: .monospaced)).disabled(model.exhibitProtected)
                        .foregroundStyle(model.exhibitProtected ? .secondary : .primary)
                        .focused($focus, equals: .exhb).onSubmit(submit).accessibilityIdentifier("inExhb")
                        .onChange(of: inExhb) { _, v in if v.count > 9 { inExhb = String(v.prefix(9)) } }
                }
                FieldRow(step: 3, label: "Third, type the Award ID you are selecting", hint: "3 digits (see the award list below)", error: result?.awardErr ?? false) {
                    TextField("", text: $inputAward).keyboardType(.numberPad).font(.system(.body, design: .monospaced))
                        .focused($focus, equals: .award).onSubmit(submit).accessibilityIdentifier("inputAward")
                        .onChange(of: inputAward) { _, v in if v.count > 3 { inputAward = String(v.prefix(3)) } }
                }
                ErrorLine(text: result?.errLine)
            }
            Card {
                Text("Available Awards for This Year:").font(.headline)
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
                Text("You may only vote for ONE award. Choose your nomination carefully!").foregroundStyle(Theme.danger).fontWeight(.semibold)
            }
            FKeyBar(legend: "TAB = Switch Fields · ENTER = Submit") {
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
        Screen(legacyPath: "ADDVOTE (VOTESCR/VOTEEND)") {
            Card {
                Text("THANK YOU FOR VOTING!").font(.system(size: 34, weight: .black)).foregroundStyle(Theme.brand)
                    .accessibilityIdentifier("screenTitle")
                Text("Your vote has been RECORDED!").fontWeight(.bold).foregroundStyle(Theme.ok)
                Text("Thank you for participating in the awards show for Vintage Computer Festival Southeast 2024! Enjoy the rest of our exhibits and the rest of the Southern Fried Gaming Expo!")
                Grid(alignment: .leading) {
                    GridRow { Text("Badge").foregroundStyle(.secondary); Text("\(badge)").monospaced().accessibilityIdentifier("out.badge") }
                    GridRow { Text("Exhibit").foregroundStyle(.secondary); Text(exhibit).monospaced().accessibilityIdentifier("out.exhibit") }
                    GridRow { Text("Award").foregroundStyle(.secondary); Text(String(format: "%03d", award)).monospaced().accessibilityIdentifier("out.award") }
                }
            }
            FKeyBar(legend: "Press ENTER to return to the main menu.") {
                FKeyButton(title: "メニューへ戻る", key: "ENTER", primary: true) { model.returnToCaller() }
            }
        }
    }
}

/// ENDOFCON (B-03): 投票期間終了
struct EndOfConView: View {
    @EnvironmentObject var model: AppModel
    var body: some View {
        Screen(legacyPath: "ADDVOTE (VOTESCR/ENDOFCON)") {
            Card {
                Text("SORRY!").font(.system(size: 34, weight: .black)).foregroundStyle(Theme.danger).accessibilityIdentifier("screenTitle")
                Text("The voting period has ended and you can no longer vote.").fontWeight(.bold)
                Text("However, you may sign this exhibit guestbook if you would like.")
            }
            FKeyBar(legend: "Press ENTER to exit.") {
                FKeyButton(title: "終了", key: "ENTER", primary: true) { model.returnToCaller() }
            }
        }
    }
}
