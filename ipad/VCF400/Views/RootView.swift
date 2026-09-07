import SwiftUI
import VCF400Kit

struct RootView: View {
    @EnvironmentObject var model: AppModel

    var body: some View {
        NavigationStack(path: $model.path) {
            MainMenuView()
                .navigationDestination(for: Route.self) { route in
                    destination(route)
                }
        }
    }

    @ViewBuilder
    private func destination(_ route: Route) -> some View {
        switch route {
        case .navigate(let next): NavigateView(next: next)
        case .vote: VoteView()
        case .voteEnd(let badge, let exhibit, let award): VoteEndView(badge: badge, exhibit: exhibit, award: award)
        case .endOfCon: EndOfConView()
        case .addComment: AddCommentView()
        case .endComment(let id): EndCommentView(id: id)
        case .readComment: ReadCommentView()
        case .learn: LearnView()
        case .kiosk(let exhibit): KioskView(exhibitId: exhibit)
        case .admPswrd(let exhibit): AdmPswrdView(exhibitId: exhibit)
        case .signoff: SignoffView()
        }
    }
}

/// 全画面共通の背景・上部バー (サインオンユーザー表示)
struct Screen<Content: View>: View {
    @EnvironmentObject var model: AppModel
    let screen: String
    let legacyPath: String
    @ViewBuilder var content: Content
    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                HStack {
                    Text("VCF/400").font(.headline.bold()).foregroundStyle(Theme.brand)
                    Text(screen).font(.system(.caption, design: .monospaced)).foregroundStyle(.secondary)
                        .accessibilityIdentifier("screen")
                    Text(legacyPath).font(.system(.caption, design: .monospaced)).foregroundStyle(.secondary)
                    Spacer()
                    Text(L10n.signedOnAs + " ").font(.caption).foregroundStyle(.secondary) +
                    Text(model.launch.profile).font(.system(.caption, design: .monospaced).bold()) +
                    Text(model.launch.isShared ? L10n.sharedTerminal : "").font(.caption).foregroundStyle(.secondary)
                }
                content
            }
            .padding(24)
            .frame(maxWidth: 1000)
            .frame(maxWidth: .infinity)
        }
        .background(Theme.bg)
        .navigationBarTitleDisplayMode(.inline)
        .toolbarBackground(Theme.brand, for: .navigationBar)
        .toolbarBackground(.visible, for: .navigationBar)
        .toolbarColorScheme(.dark, for: .navigationBar)
        .navigationBarBackButtonHidden(true)
    }
}

/// 画面下部のファンクションキー列
struct FKeyBar<Content: View>: View {
    var legend: String? = nil
    @ViewBuilder var content: Content
    var body: some View {
        HStack(spacing: 10) {
            if let l = legend { Text(l).font(.caption).foregroundStyle(.secondary) }
            Spacer()
            content
        }
        .padding(.top, 8)
    }
}
