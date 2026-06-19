import { nanoid } from 'nanoid'

export function generateTraceId(): string {
  return nanoid()
}
