import React, { useCallback, useEffect, useState } from "react";

/**
 * Mirrors GET /api/v1/assignments/courses/{course_id} assignment rows.
 * When the student sends Authorization: Bearer, the API sets my_completed_at / my_score.
 */
export type StudentAssignmentActivity = {
  activity_id: string;
  topic?: string;
  subtopic?: string;
  description?: string | null;
  content_url?: string | null;
};

export type StudentCourseAssignment = {
  id: string;
  course_id: string;
  activity_id: string;
  due_date?: string | null;
  title_override?: string | null;
  created_at: string;
  activity?: StudentAssignmentActivity | null;
  course_name?: string | null;
  my_completed_at?: string | null;
  my_score?: number | null;
};

export function getAssignmentContentUrl(
  assignment: StudentCourseAssignment,
): string | null {
  const raw = assignment.activity?.content_url;
  if (typeof raw !== "string") return null;
  const t = raw.trim();
  return t.length > 0 ? t : null;
}

/**
 * Query params for m4u_extended.js: it POSTs to
 * {archimedes_api_base}/api/v1/assignments/{assignment_id}/completions
 * using the token from the URL hash (#access_token=...) or M4UConfig.
 */
export function buildAssignmentLaunchUrl(
  contentUrl: string,
  assignmentId: string,
  options: {
    activityId?: string;
    apiBase: string;
    studentId: string;
  },
): string {
  const u = new URL(contentUrl);
  u.searchParams.set("assignment_id", assignmentId);
  if (options.activityId) {
    u.searchParams.set("activity_id", options.activityId);
  }
  u.searchParams.set(
    "archimedes_api_base",
    options.apiBase.replace(/\/$/, ""),
  );
  u.searchParams.set("student_id", options.studentId);
  return u.toString();
}

/** Append hash token so the miniquiz page can read it without query-string leaks. */
export function withAccessTokenHash(
  launchUrl: string,
  accessToken: string,
): string {
  const u = new URL(launchUrl);
  const hp = new URLSearchParams();
  hp.set("access_token", accessToken);
  u.hash = hp.toString();
  return u.toString();
}

/** Label for the primary link: completed students review the same content_url. */
export function getAssignmentPrimaryLabel(
  assignment: StudentCourseAssignment,
): "Open assignment" | "Review assignment" {
  return assignment.my_completed_at ? "Review assignment" : "Open assignment";
}

type ListResponse = { assignments: StudentCourseAssignment[]; total: number };

export type StudentAssignmentsPageProps = {
  courseId: string;
  /** e.g. https://api.example.com (no trailing slash) */
  apiBase: string;
  accessToken?: string | null;
  /** From GET /auth/me student profile.id — required for quiz launch URL */
  studentId?: string | null;
};

/**
 * Student assignments: opens miniquiz with params + token hash for direct API completion.
 */
export function StudentAssignmentsPage({
  courseId,
  apiBase,
  accessToken,
  studentId,
}: StudentAssignmentsPageProps) {
  const [assignments, setAssignments] = useState<StudentCourseAssignment[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    const url = `${apiBase.replace(/\/$/, "")}/api/v1/assignments/courses/${courseId}`;
    const headers: Record<string, string> = {
      Accept: "application/json",
    };
    if (accessToken) {
      headers.Authorization = `Bearer ${accessToken}`;
    }
    try {
      const res = await fetch(url, { headers });
      if (!res.ok) {
        setError(`Could not load assignments (${res.status})`);
        setAssignments([]);
        return;
      }
      const data = (await res.json()) as ListResponse;
      setAssignments(data.assignments ?? []);
    } catch {
      setError("Could not load assignments");
      setAssignments([]);
    } finally {
      setLoading(false);
    }
  }, [apiBase, courseId, accessToken]);

  useEffect(() => {
    void load();
  }, [load]);

  const canBuildQuizLink = Boolean(
    accessToken && studentId && apiBase,
  );

  return (
    <section style={{ maxWidth: 640, margin: "0 auto", padding: "1rem" }}>
      <h1 style={{ fontSize: "1.25rem", marginBottom: "0.5rem" }}>
        Assignments
      </h1>
      <p style={{ fontSize: "0.875rem", color: "#444", marginBottom: "0.75rem" }}>
        Assignments open in a <strong>new tab</strong>. On submit, the miniquiz
        calls <code>POST .../assignments/&#123;id&#125;/completions</code> on your
        API (token in URL hash). Add the miniquiz CDN origin to backend{" "}
        <code>CORS_ORIGINS</code>.
      </p>
      {!canBuildQuizLink && (
        <p style={{ fontSize: "0.8rem", color: "#a60" }}>
          Provide <code>accessToken</code> and <code>studentId</code> so launch
          URLs include API base, student id, and hash token.
        </p>
      )}

      {loading && <p>Loading…</p>}
      {error && <p role="alert">{error}</p>}

      {!loading && !error && assignments.length === 0 && (
        <p>No assignments for this course yet.</p>
      )}

      <ul style={{ listStyle: "none", padding: 0, margin: 0 }}>
        {assignments.map((a) => {
          const title =
            a.title_override?.trim() ||
            a.activity?.description?.trim() ||
            a.activity_id;
          const baseHref = getAssignmentContentUrl(a);
          const label = getAssignmentPrimaryLabel(a);
          let href: string | null = null;
          if (baseHref && canBuildQuizLink) {
            const launch = buildAssignmentLaunchUrl(baseHref, a.id, {
              activityId: a.activity_id,
              apiBase,
              studentId: studentId as string,
            });
            href = withAccessTokenHash(launch, accessToken as string);
          } else {
            href = baseHref;
          }

          return (
            <li
              key={a.id}
              style={{
                border: "1px solid #ddd",
                borderRadius: 8,
                padding: "0.75rem 1rem",
                marginBottom: "0.75rem",
              }}
            >
              <div style={{ fontWeight: 600, marginBottom: "0.35rem" }}>
                {title}
              </div>
              {a.activity?.topic && (
                <div style={{ fontSize: "0.8rem", color: "#555" }}>
                  {a.activity.topic}
                  {a.activity.subtopic ? ` · ${a.activity.subtopic}` : ""}
                </div>
              )}
              <div style={{ marginTop: "0.5rem" }}>
                {href ? (
                  <a
                    href={href}
                    target="_blank"
                    rel="noopener noreferrer"
                    style={{ fontWeight: 500 }}
                  >
                    {label}
                  </a>
                ) : (
                  <span style={{ color: "#777" }}>Link unavailable</span>
                )}
              </div>
            </li>
          );
        })}
      </ul>
    </section>
  );
}
